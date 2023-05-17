{

    // ===== Contract Information ===== //
    // Name: Player Proxy
    // Description: This contract guards the Player Proxy box, which holds the player's card token.
    //              The logic of this contract determines how many game tokens the player will receive
    //              based on the card they hold.
    // Version: 1.0.0
    // Author: Luca D'Angelo (ldgaetano@protonmail.com)

    // ===== Box Contents ===== //
    // Tokens: Coll[(Coll[Byte], Long)]
    // 1. (CardTokenId, 1)
    // Registers:
    // R4: SigmaProp  PlayerPK
    // R5: Coll[Byte] GameLPSingletonTokenId
    // R6: Coll[Byte] GameTokenId
    // R7: Coll[Byte] CardValueMappingSingletonTokenId

    // ===== Relevant Transactions ===== //
    // 1. Trade-In Tx
    // Inputs: GameLP, PlayerProxy
    // DataInputs: CardValueMapping
    // Outputs: GameLP, PlayerPK, DevAddress, TxOperator, MinerFee
    // Context Variables: CardTokenIssuerBox, CardSetCollectionIssuerBox
    
    // ===== Compile Time Constants ($) ===== //
    // $MinerFee

    // ===== Context Variables (_) ===== //
    // _CardTokenIssuerBox: Box
    // _CardSetCollectionIssuerBox: Box

    // ===== Relevant Variables ===== //
    val _CardTokenIssuerBox: Box = getVar[Box](0).get
    val _CardSetCollectionIssuerBox: Box = getVar[Box](1).get
    val cardSetCollectionTokenId: Coll[Byte] = _CardTokenIssuerBox.R7[Coll[Byte]].get
    val cardTokenId: Coll[Byte] = SELF.tokens(0)._1
    val playerPK: SigmaProp = SELF.R4[SigmaProp].get
    val gameLPSingletonTokenId: Coll[Byte] = SELF.R5[Coll[Byte]].get
    val gameTokenId: Coll[Byte] = SELF.R6[Coll[Byte]].get
    val cardValueMappingSingletonTokenId: Coll[Byte] = SELF.R7[Coll[Byte]].get

    // ===== User Defined Functions ===== //
    def selectRegister(bucket: Byte): Coll[(Coll[Byte], Long)] = {

        val cardValueMappingBoxIN: Box = CONTEXT.dataInputs(0)

        if (bucket == 0) {
            cardValueMappingBoxIN.R5[Coll[(Coll[Byte], Long)]].get
        } else if (bucket == 1) {
            cardValueMappingBoxIN.R6[Coll[(Coll[Byte], Long)]].get
        } else if (bucket == 2) {
            cardValueMappingBoxIN.R7[Coll[(Coll[Byte], Long)]].get
        } else if (bucket == 3) {
            cardValueMappingBoxIN.R8[Coll[(Coll[Byte], Long)]].get
        } else {
            cardValueMappingBoxIN.R9[Coll[(Coll[Byte], Long)]].get
        }

    }

    // ===== Trade-In Tx ===== //
    val validTradeInTx: Boolean = {

        // Inputs
        val gameLPBoxIN: Box = INPUTS(0)

        // DataInputs
        val cardValueMappingBoxIN: Box = CONTEXT.dataInputs(0)
        val cardValueMappingBoxData: (Coll[Byte], (Coll[Byte], (Long, (Byte, Byte)))) = cardValueMappingBoxIN.R4[(Coll[Byte], (Coll[Byte], (Long, (Byte, Byte))))].get

        // Outputs
        val gameLPBoxOUT: Box = OUTPUTS(0)
        val playerPKBoxOUT: Box = OUTPUTS(1)
        val devAddressBoxOUT: Box = OUTPUTS(2)
        val txOperatorBoxOUT: Box = OUTPUTS(3)
        val minerFeeBoxOUT: Box = OUTPUTS(4)

        // Inputs checks
        val validInputs: Boolean = {

            val validPlayerTokenId: Boolean = (_CardTokenIssuerBox.id == cardTokenId)
            val validPlayerTokenIssuer: Boolean = _CardTokenIssuerBox.tokens.exists({ (t: (Coll[Byte], Long)) => (t._1 == cardSetCollectionTokenId) })
            val validCardSetCollectionIssuer: Boolean = (_CardSetCollectionIssuerBox.id == cardSetCollectionTokenId)

            val validCardValueMappingBoxIN: Boolean = {

                val cardValueMappingCardSetCollectionTokenId: Coll[Byte] = cardValueMappingBoxData._1
                val cardValueMappingGameTokenId: Coll[Byte] = cardValueMappingBoxData._2._1

                val validSingletonTokenId: Boolean = (cardValueMappingBoxIN.tokens(0) == (cardValueMappingSingletonTokenId, 1L))
                val validCardSetCollectionTokenId: Boolean = (cardValueMappingCardSetCollectionTokenId == cardSetCollectionTokenId)
                val validGameTokenId: Boolean = (cardValueMappingGameTokenId == gameTokenId)

                allOf(Coll(
                    validSingletonTokenId,
                    validCardSetCollectionTokenId,
                    validGameTokenId
                ))

            }

            val validGameLPBoxIN: Boolean = {

                val validGameLPBoxSingletonToken: Boolean = (gameLPBoxIN.tokens(0) == (gameLPSingletonTokenId, 1L))
                val validGameTokenId: Boolean = if (gameLPBoxIN.tokens.size == 2) (gameLPBoxIN.tokens(1)._1 == gameTokenId) else false

                allOf(Coll(
                    validGameLPBoxSingletonToken,
                    validGameTokenId
                ))

            }

            allOf(Coll(
                validPlayerTokenId,
                validPlayerTokenIssuer,
                validCardSetCollectionIssuer,
                validCardValueMappingBoxIN,
                validGameLPBoxIN
            ))

        }

        // Outputs checks
        val validOutputs: Boolean = {

            val validPlayerPKBoxOUT: Boolean = {

                val validValue: Boolean = (playerPKBoxOUT.value == SELF.value - $MinerFee)
                val validContract: Boolean = (playerPKBoxOUT.propositionBytes == playerPK.propBytes)

                val validGameTokens: Boolean = {
                    
                    // Card Value Mapping box data
                    val totalDataBuckets: Byte = cardValueMappingBoxData._2._2._2._1
                    val dataBucketSize: Byte = cardValueMappingBoxData._2._2._2._2
                    val playerCardHash: Coll[Byte] = blake2b256(cardTokenId)
                    val playerCardHashNum: Int = byteArrayToLong(playerCardHash).toInt // Int or Long
                    
                    // Game LP box data
                    val emissionInterval: Long = gameLPBoxIN.R5[Long].get
                    val emissionReductionFactorMultiplier: Long = gameLPBoxIN.R6[Long].get
                    val emissionReductionFactor: Long = gameLPBoxIN.R7[Long].get
                    val cardTokenBurnCount: Long = gameLPBoxIN.R8[Long].get
                    val cardTokenBurnTotal: Long = gameLPBoxIN.R9[Long].get

                    val dataBucket: Byte = (playerCardHashNum % totalDataBuckets).toByte // Remember that the data buckets start at register R5
                    val dataBucketIndex: Byte = (playerCardHashNum % dataBucketSize).toByte

                    val register: Coll[(Coll[Byte], Long)] = selectRegister(dataBucket)
                    val entry: (Coll[Byte], Long) = register(dataBucketIndex.toInt)
                    val cardHash: Coll[Byte] = entry._1
                    val maxCardValue: Long = entry._2
                    
                    val validCardHash: Boolean = (cardHash == playerCardHash)
                    
                    val validGameTokenTransfer: Boolean = {

                        if (cardTokenBurnCount < emissionInterval) {
                            
                            val cardValue: Long = maxCardValue / emissionReductionFactor
                            val newCount: Long = cardTokenBurnCount + 1L

                            val validTransfer: Boolean = (playerPKBoxOUT.tokens(0) == (gameTokenId, cardValue))

                            val validGameLPBoxOUTRegisterUpdate: Boolean = {

                                allOf(Coll(
                                    (gameLPBoxOUT.R7[Long].get == emissionReductionFactor),
                                    (gameLPBoxOUT.R8[Long].get == cardTokenBurnCount + 1L),
                                    (gameLPBoxOUT.R9[Long].get == cardTokenBurnTotal + 1L)
                                ))

                            }

                            allOf(Coll(
                                validTransfer,
                                validGameLPBoxOUTRegisterUpdate
                            ))

                        } else {

                            val newCount: Long = 1L
                            val newFactor: Long = emissionReductionFactorMultiplier * emissionReductionFactor
                            val newCardValue: Long = maxCardValue / newFactor

                            val validTransfer: Boolean = (playerPKBoxOUT.tokens(0) == (gameTokenId, newCardValue))

                            val validGameLPBoxOUTRegisterUpdate: Boolean = {

                                allOf(Coll(
                                    (gameLPBoxOUT.R7[Long].get == newFactor),
                                    (gameLPBoxOUT.R8[Long].get == newCount),
                                    (gameLPBoxOUT.R9[Long].get == cardTokenBurnTotal + 1L)
                                ))
                        
                            }

                            allOf(Coll(
                                validTransfer,
                                validGameLPBoxOUTRegisterUpdate
                            ))

                        }

                    }
                    

                    allOf(Coll(
                        validCardHash,
                        validGameTokenTransfer
                    ))

                }

                val validCardTokenBurn: Boolean = OUTPUTS.forall({ (output: Box) =>

                    output.tokens.forall({ (t: (Coll[Byte], Long)) =>

                        t._1 != cardTokenId

                    })

                })

                allOf(Coll(
                    validValue,
                    validContract,
                    validGameTokens,
                    validCardTokenBurn
                ))

            }

            val validMinerFeeBoxOUT: Boolean = (minerFeeBoxOUT.value == $MinerFee)

            allOf(Coll(
                validPlayerPKBoxOUT,
                validMinerFeeBoxOUT
            ))

        }

        allOf(Coll(
            validInputs,
            validOutputs
        ))

    }

    sigmaProp(validTradeInTx)

}