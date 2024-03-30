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
    // R4: GroupElement     PlayerPK
    // R5: Coll[Byte]       GameLPSingletonTokenId
    // R6: Coll[Byte]       GameTokenId
    // R7: Coll[Byte]       CardValueMappingTokenId
    // R8: Long             MinerFee

    // ===== Relevant Transactions ===== //
    // 1. Trade-In Tx
    // Inputs: GameLP, PlayerProxy
    // DataInputs: CardValueMapping
    // Outputs: GameLP, PlayerPK, DevAddress, TxOperator, MinerFee
    // Context Variables: CardTokenIssuerBox, CardSetCollectionIssuerBox
    
    // ===== Compile Time Constants ($) ===== //
    // None

    // ===== Context Variables (_) ===== //
    // _CardSetCollectionIssuerBox: Box
    // _CardTokenIssuerBox: Box

    // ===== Relevant Variables ===== //
    val cardTokenId: Coll[Byte]                     = SELF.tokens(0)._1
    val playerPKGE: GroupElement                    = SELF.R4[GroupElement]get
    val playerPK: SigmaProp                         = proveDlog(playerPKGE)
    val gameLPSingletonTokenId: Coll[Byte]          = SELF.R5[Coll[Byte]].get
    val gameTokenId: Coll[Byte]                     = SELF.R6[Coll[Byte]].get
    val cardValueMappingTokenId: Coll[Byte]         = SELF.R7[Coll[Byte]].get
    val minerFee: Long                              = SELF.R8[Long].get
    val _CardSetCollectionIssuerBox: Box            = getVar[Box](0).get
    val _CardTokenIssuerBox: Box                    = getVar[Box](1).get
    val cardSetCollectionTokenId: Coll[Byte]        = _CardTokenIssuerBox.R7[Coll[Byte]].get

    // ===== Trade-In Tx ===== //
    val validTradeInTx: Boolean = {

        // Inputs
        val gameLPBoxIN: Box                            = INPUTS(0)
        val devFee: (Long, Long)                        = gameLPBoxIN.R4[(Coll[(Long, Long)], (Coll[Byte], Coll[Byte]))].get._1(0)
        val txOperatorFee: (Long, Long)                 = gameLPBoxIN.R4[(Coll[(Long, Long)], (Coll[Byte], Coll[Byte]))].get._1(1)
        val emissionInterval: Long                      = gameLPBoxIN.R5[Long].get
        val emissionReductionFactorMultiplier: Long     = gameLPBoxIN.R6[Long].get
        val emissionReductionFactor: Long               = gameLPBoxIN.R7[Long].get
        val cardTokenBurnCount: Long                    = gameLPBoxIN.R8[Long].get
        val cardTokenBurnTotal: Long                    = gameLPBoxIN.R9[Long].get

        // DataInputs
        val cardValueMappingBoxIN: Box = CONTEXT.dataInputs(0)
        
        // Outputs
        val gameLPBoxOUT: Box       = OUTPUTS(0)
        val playerPKBoxOUT: Box     = OUTPUTS(1)
        val devAddressBoxOUT: Box   = OUTPUTS(2)
        val txOperatorBoxOUT: Box   = OUTPUTS(3)
        val minerFeeBoxOUT: Box     = OUTPUTS(4)

        // Inputs checks
        val validInputs: Boolean = {

            val validCardTokenId: Boolean = (_CardTokenIssuerBox.id == cardTokenId)
            val validCardTokenIssuer: Boolean = _CardTokenIssuerBox.tokens.exists({ (t: (Coll[Byte], Long)) => (t._1 == cardSetCollectionTokenId) })
            val validCardSetCollectionIssuer: Boolean = (_CardSetCollectionIssuerBox.id == cardSetCollectionTokenId)

            val validCardValueMappingBoxIN: Boolean = {

                val validCardValueMappingTokenId: Boolean = (cardValueMappingBoxIN.tokens(0) == (cardValueMappingTokenId, 1L))
                val validCardSetCollectionTokenId: Boolean = (cardValueMappingCardSetCollectionTokenId == cardSetCollectionTokenId)
                val validCardTokenId: Boolean = (cardValueMappingCardTokenId == cardTokenId)
                val validGameTokenId: Boolean = (cardValueMappingGameTokenId == gameTokenId)

                allOf(Coll(
                    validCardValueMappingTokenId,
                    validCardSetCollectionTokenId,
                    validCardTokenId,
                    validGameTokenId
                ))

            }

            val validGameLPBoxIN: Boolean = {

                val validGameLPBoxSingletonToken: Boolean = (gameLPBoxIN.tokens(0) == (gameLPSingletonTokenId, 1L))
                val validGameTokenId: Boolean = if (gameLPBoxIN.tokens.size >= 3) (gameLPBoxIN.tokens(1)._1 == gameTokenId) else false

                allOf(Coll(
                    validGameLPBoxSingletonToken,
                    validGameTokenId
                ))

            }

            allOf(Coll(
                validCardTokenId,
                validCardTokenIssuer,
                validCardSetCollectionIssuer,
                validCardValueMappingBoxIN,
                validGameLPBoxIN
            ))

        }

        // Outputs checks
        val validOutputs: Boolean = {

            val validPlayerPKBoxOUT: Boolean = {

                val validValue: Boolean = (playerPKBoxOUT.value == SELF.value - minerFee)
                val validContract: Boolean = (playerPKBoxOUT.propositionBytes == playerPK.propBytes)

                val validGameTokens: Boolean = {

                    val maxCardValue: Long = cardValueMappingCardValue
                                      
                    val validGameTokenTransfer: Boolean = {

                        if (cardTokenBurnCount < emissionInterval) { // We do not reduced the value of the card yet
                            
                            val cardValue: Long = maxCardValue / emissionReductionFactor
                            val newCount: Long = cardTokenBurnCount + 1L

                            val devFeeAmount: Long = (cardValue * devFee._1) / devFee._2
                            val txOperatorFeeAmount: Long = (cardValue * txOperatorFee._1) / txOperatorFee._2
                            val playerAmount: Long = cardValue - devFeeAmount - txOperatorFeeAmount

                            val validPlayerAmountTransfer: Boolean = (playerPKBoxOUT.tokens(0) == (gameTokenId, playerAmount))

                            val validDevFeeTransfer: Boolean = (devAddressBoxOUT.tokens(0) == (gameTokenId, devFeeAmount))
                            
                            val validTxOperatorFeeTransfer: Boolean = (txOperatorBoxOUT.tokens(0) == (gameTokenId, txOperatorFeeAmount))

                            val validGameLPBoxOUTRegisterUpdate: Boolean = {

                                allOf(Coll(
                                    (gameLPBoxOUT.R7[Long].get == emissionReductionFactor),
                                    (gameLPBoxOUT.R8[Long].get == cardTokenBurnCount + 1L),
                                    (gameLPBoxOUT.R9[Long].get == cardTokenBurnTotal + 1L)
                                ))

                            }

                            allOf(Coll(
                                validPlayerAmountTransfer,
                                validDevFeeTransfer,
                                validTxOperatorFeeTransfer,
                                validGameLPBoxOUTRegisterUpdate
                            ))

                        } else { // We now reduce the value of the card

                            val newCount: Long = 1L
                            val newFactor: Long = emissionReductionFactorMultiplier * emissionReductionFactor
                            val newCardValue: Long = maxCardValue / newFactor

                            val devFeeAmount: Long = (newCardValue * devFee._1) / devFee._2
                            val txOperatorFeeAmount: Long = (newCardValue * txOperatorFee._1) / txOperatorFee._2
                            val playerAmount: Long = newCardValue - devFeeAmount - txOperatorFeeAmount

                            val validPlayerAmountTransfer: Boolean = (playerPKBoxOUT.tokens(0) == (gameTokenId, playerAmount))

                            val validDevFeeTransfer: Boolean = (devAddressBoxOUT.tokens(0) == (gameTokenId, devFeeAmount))
                            
                            val validTxOperatorFeeTransfer: Boolean = (txOperatorBoxOUT.tokens(0) == (gameTokenId, txOperatorFeeAmount))

                            val validGameLPBoxOUTRegisterUpdate: Boolean = {

                                allOf(Coll(
                                    (gameLPBoxOUT.R7[Long].get == newFactor),
                                    (gameLPBoxOUT.R8[Long].get == newCount),
                                    (gameLPBoxOUT.R9[Long].get == cardTokenBurnTotal + 1L)
                                ))
                        
                            }

                            allOf(Coll(
                                validPlayerAmountTransfer,
                                validDevFeeTransfer,
                                validTxOperatorFeeTransfer,
                                validGameLPBoxOUTRegisterUpdate
                            ))

                        }

                    }
                    
                    allOf(Coll(
                        validGameTokenTransfer
                    ))

                }

                val validCardTokenBurn: Boolean = OUTPUTS.forall({ (output: Box) =>

                    output.tokens.forall({ (t: (Coll[Byte], Long)) => (t._1 != cardTokenId) })

                })

                allOf(Coll(
                    validValue,
                    validContract,
                    validGameTokens,
                    validCardTokenBurn
                ))

            }

            val validMinerFeeBoxOUT: Boolean = (minerFeeBoxOUT.value == minerFee)

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