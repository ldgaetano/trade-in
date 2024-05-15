{

    // ===== Contract Information ===== //
    // Name: Game LP
    // Description: This contract guards the Game LP box, which holds the game tokens. This contract ensures that 
    //              tokens can be withdrawn from the LP only when the Card Value Mapping and Player Proxy boxes are inputs.
    //              The Player Proxy contract determines the correct value that must be withdrawn from the LP.
    //              This ensures that the protocol stays modular by enabling the structure of the Game LP contract to remain
    //              constant, while only requiring the Player Proxy contract to be modified.
    // Version: 1.0.0
    // Author: Luca D'Angelo (ldgaetano@protonmail.com)

    // ===== Box Contents ===== //
    // Tokens: Coll[(Coll[Byte], Long)]
    // 1. (GameLPSingletonTokenId, 1)
    // 2. (GameTokenId, GameTokenAmount + 1) // The extra token is there to make calculations within the contract easier.
    // 3. (CardValueMappingToken, 1)
    // Registers:
    // R4: Coll[(Long, Long)] Coll(TradeInFee, DevFee)
    // R5: Long       EmissionInterval
    // R6: Long       EmissionReductionFactorMultiplier
    // R7: Long       EmissionReductionFactor
    // R8: Long       CardTokenBurnCount
    // R9: Long       CardTokenBurnTotal
    
    // ===== Relevant Transactions ===== //
    // 1. Trade In Tx
    // Inputs: GameLP, PlayerProxy
    // DataInputs: CardValueMapping
    // Outputs: GameLP, TradeInFee, DevFee, PlayerPK, TxOperator, MinerFee
    // Context Variables: TxType, CardSetCollectionIssuerBox, CardTokenIssuerBox
    // 2. Card-Value-Mapping Box Creation Tx
    // Inputs: GameLP, CardValueMappingIssuance
    // DataInputs: None
    // Outputs: GameLP, CardValueMapping1, ... , CardValueMappingN, MinerFee
    // Context Variables: TxType
    // 3. Storage Rent Top Up Tx
    // Inputs: GameLP, StorageRentTopUp
    // DataInputs: None
    // Outputs: GameLP, MinerFee
    // Context Variables: TxType
    
    // ===== Compile Time Constants ($) ===== //
    // $CardValueMappingContractBytes: Coll[Byte]
    // $DevPKGE: GroupElement
    // $DevAddress: Coll[Byte]
    // $TradeInFeeAddress: Coll[Byte]
    // $SetCreationMultiSigThreshold: Int
    // $SetCreationMultiSigAddressesGE: Coll[GroupElement]

    // ===== Context Variables (_) ===== //
    // _TxType: Byte
    // _CardSetCollectionIssuerBox: Box
    // _CardTokenIssuerBox: Box

    // ===== Transaction Types ===== //
    // 1 => Trade-In Tx
    // 2 => Card-Value-Mapping Box Creation Tx
    // 3 => Storage Rent Top-Up

    // ===== User Defined Functions ===== //
    // divUp: (Long, Long) => BigInt

    // Integer division, rounded up.
    def divUp(operands: (Long, Long)): Long = {

        val a: Long = operands._1 // Dividend
        val b: Long = operands._2 // Divisor

        if (b == 0L) {
            -1L
        } else {
            (a + (b-1L)) / b
        }

    } 

    // ===== Relevant Variables ===== //
    val gameLPSingletonToken: (Coll[Byte], Long)        = SELF.tokens(0)
    val gameTokenId: Coll[Byte]                         = SELF.tokens(1)._1
    val tradeInFee: (Long, Long)                        = SELF.R4[Coll[(Long, Long)]].get(0)
    val devFee: (Long, Long)                            = SELF.R4[Coll[(Long, Long)]].get(1) 
    val emissionInterval: Long                          = SELF.R5[Long].get
    val emissionReductionFactorMultiplier: Long         = SELF.R6[Long].get
    val emissionReductionFactor: Long                   = SELF.R7[Long].get
    val cardTokenBurnCount: Long                        = SELF.R8[Long].get
    val cardTokenBurnTotal: Long                        = SELF.R9[Long].get
    val _TxType: Byte                                   = getVar[Byte](0).get
    val devPK: SigmaProp                                = proveDlog($DevPKGE)
    val setCreationMultiSigAddresses: Coll[SigmaProp]   = $SetCreationMultiSigAddressesGE.map({ (ge: GroupElement) => proveDlog(ge) })

    if (_TxType == 1) {

        // ===== Trade-In Tx ===== //
        val validTradeInTx: Boolean = {

            // Relevant Variables
            val _CardSetCollectionIssuerBox: Box        = getVar[Box](1).get
            val _CardTokenIssuerBox: Box                = getVar[Box](2).get
            val cardSetCollectionTokenId: Coll[Byte]    = _CardTokenIssuerBox.R7[Coll[Byte]].get

            // Check if there is anything left in the bank
            if (SELF.tokens(1)._2 == 1) {
                false
            } else {

                // DataInputs
                val cardValueMappingBoxIN: Box                              = CONTEXT.dataInputs(0)
                val cardValueMappingGameTokenId: Coll[Byte]                 = cardValueMappingBoxIN.R4[Coll[Byte]].get
                val cardValueMappingCardSetCollectionTokenId: Coll[Byte]    = cardValueMappingBoxIN.R5[Coll[Byte]].get
                val cardValueMappingCardTokenId: Coll[Byte]                 = cardValueMappingBoxIN.R6[Coll[Byte]].get
                val cardValueMappingCardValue: Long                         = cardValueMappingBoxIN.R7[Long].get // The max possible card value.

                // Outputs
                val gameLPBoxOUT: Box       = OUTPUTS(0)
                // val tradeInFeeBoxOUT: Box   = OUTPUTS(1)
                // val devFeeBoxOUT: Box       = OUTPUTS(2)

                val validCard: Boolean = {

                    // Conditions:
                    // 1. Card value mapping card token exists in the inputs somewhere
                    // 2. Card token issuer box (from minting) containts the card set collection token referenced from the standard in its tokens (required for the collection standard EIP34)
                    // 3. Card set collection issuer box id matches the token id of the card set collection token
                    // This ensures that the card id is not a random id but a legit id that comes from minting a token that is part of a collection, according to EIP34.

                    val validCardToken: Boolean = INPUTS.exists({ (input: Box) => input.tokens.exists({ (t: (Coll[Byte], Long)) => t == (_CardTokenIssuerBox.id, 1L) }) })
                    val validCardTokenIssuer: Boolean = _CardTokenIssuerBox.tokens.exists({ (t: (Coll[Byte], Long)) => (t._1 == cardSetCollectionTokenId) })
                    val validCardSetCollectionIssuer: Boolean = (_CardSetCollectionIssuerBox.id == cardSetCollectionTokenId)

                    allOf(Coll(
                        validCardToken,
                        validCardTokenIssuer,
                        validCardSetCollectionIssuer
                    ))

                }

                val validCardValueMappingBoxIN: Boolean = {

                    val cardValueMappingToken: (Coll[Byte], Long) = cardValueMappingBoxIN.tokens(0)

                    // Conditions:
                    // 1. We have a legit card value mapping box with the correct contract
                    // 2. We have a legit card value mapping token
                    // 3. The card value mapping box is associated to a legit card set
                    // 4. The card value mapping box is associated to a legit card
                    // 5. The card value mapping box is associated to the correct game token

                    val validContract: Boolean = (cardValueMappingBoxIN.propositionBytes == $CardValueMappingContractBytes) // This contract is written without compile time constants and is thus always identical.
                    val validCardValueMappingToken: Boolean = SELF.tokens.exists({ (t: (Coll[Byte], Long)) => (t == cardValueMappingToken) })
                    val validCardSetCollectionTokenId: Boolean = (cardValueMappingCardSetCollectionTokenId == _CardSetCollectionIssuerBox.id)
                    val validCardTokenId: Boolean = (cardValueMappingCardTokenId == _CardTokenIssuerBox.id)
                    val validGameTokenId: Boolean = (cardValueMappingGameTokenId == gameTokenId)

                    allOf(Coll(
                        validContract,
                        validCardValueMappingToken,
                        validCardSetCollectionTokenId,
                        validCardTokenId,
                        validGameTokenId
                    ))

                }

                val validGameLPBoxOUT: Boolean = {

                    val validSelfRecreation: Boolean = {

                        allOf(Coll(
                            (gameLPBoxOUT.value == SELF.value),
                            (gameLPBoxOUT.tokens(0) == SELF.tokens(0)),
                            (gameLPBoxOUT.tokens(1)._1 == SELF.tokens(1)._1),
                            (gameLPBoxOUT.tokens(1)._2 >= 1L),
                            (gameLPBoxOUT.tokens.slice(2, gameLPBoxOUT.tokens.size) == SELF.tokens.slice(2, SELF.tokens.size)),
                            (gameLPBoxOUT.propositionBytes == SELF.propositionBytes),
                            (gameLPBoxOUT.R4[Coll[(Long, Long)]].get == SELF.R4[Coll[(Long, Long)]].get),
                            (gameLPBoxOUT.R5[Long].get == emissionInterval),
                            (gameLPBoxOUT.R6[Long].get == emissionReductionFactorMultiplier)
                        ))

                    }
                
                    val validTokens: Boolean = {
                                        
                        val maxCardValue: Long = cardValueMappingCardValue

                        val validGameTokenTransfer: Boolean = {

                            if (cardTokenBurnCount < emissionInterval) { // We do not reduce the value of the card yet.
                                
                                val operands: (Long, Long) = (maxCardValue, emissionReductionFactor)
                                val cardValue: Long = divUp(operands)
                                val newCount: Long = cardTokenBurnCount + 1L
                                
                                val tradeInOperands: (Long, Long) = ((cardValue * tradeInFee._1), tradeInFee._2)
                                val devOperands: (Long, Long) = ((cardValue * devFee._1), devFee._2)
                                val tradeInFeeAmount: Long = if (cardValue <= 1L + tradeInFee._1 + devFee._1) 0L else divUp(tradeInOperands)
                                val devFeeAmount: Long = if (cardValue <= 1L + tradeInFee._1 + devFee._1) 0L else divUp(devOperands)
                                //val playerAmount: Long = cardValue - tradeInFeeAmount - devFeeAmount

                                val validGameLPWithdraw: Boolean = (gameLPBoxOUT.tokens(1)._2 == SELF.tokens(1)._2 - cardValue)        
                                val validTradeInFeeTransfer: Boolean = if (tradeInFeeAmount == 0L) true else (OUTPUTS(1).tokens(0) == (gameTokenId, tradeInFeeAmount))
                                val validDevFeeTransfer: Boolean = if (devFeeAmount == 0L) true else (OUTPUTS(2).tokens(0) == (gameTokenId, devFeeAmount)) 
                                
                                val validGameLPRegisterUpdate: Boolean = {

                                    allOf(Coll(
                                        (gameLPBoxOUT.R7[Long].get == emissionReductionFactor),
                                        (gameLPBoxOUT.R8[Long].get == cardTokenBurnCount + 1L),
                                        (gameLPBoxOUT.R9[Long].get == cardTokenBurnTotal + 1L)
                                    ))

                                }

                                allOf(Coll(
                                    validGameLPWithdraw,
                                    validTradeInFeeTransfer,
                                    validDevFeeTransfer,
                                    validGameLPRegisterUpdate
                                ))

                            } else { // We now reduce the value of the card

                                val newCount: Long = 1L
                                val newFactor: Long = emissionReductionFactorMultiplier * emissionReductionFactor
                                val operands: (Long, Long) = (maxCardValue, newFactor)
                                val newCardValue: Long = divUp(operands)

                                val tradeInOperands: (Long, Long) = ((newCardValue * tradeInFee._1), tradeInFee._2)
                                val devOperands: (Long, Long) = ((newCardValue * devFee._1), devFee._2)
                                val tradeInFeeAmount: Long = if (newCardValue <= 1L + tradeInFee._1 + devFee._1) 0L else divUp(tradeInOperands)
                                val devFeeAmount: Long = if (newCardValue <= 1L + tradeInFee._1 + devFee._1) 0L else divUp(devOperands)
                                //val playerAmount: Long = newCardValue - tradeInFeeAmount - devFeeAmount                   
                                
                                val validGameLPWithdraw: Boolean = (gameLPBoxOUT.tokens(1)._2 == SELF.tokens(1)._2 - newCardValue)
                                val validTradeInFeeTransfer: Boolean = if (tradeInFeeAmount == 0L) true else (OUTPUTS(1).tokens(0) == (gameTokenId, tradeInFeeAmount))
                                val validDevFeeTransfer: Boolean = if (devFeeAmount == 0L) true else (OUTPUTS(2).tokens(0) == (gameTokenId, devFeeAmount))

                                val validGameLPRegisterUpdate: Boolean = {

                                    allOf(Coll(
                                        (gameLPBoxOUT.R7[Long].get == newFactor),
                                        (gameLPBoxOUT.R8[Long].get == newCount),
                                        (gameLPBoxOUT.R9[Long].get == cardTokenBurnTotal + 1L)
                                    ))

                                }

                                validGameLPRegisterUpdate
                            
                            }

                        }

                        validGameTokenTransfer

                    }

                    val validCardTokenBurn: Boolean = {

                        OUTPUTS.forall({ (output: Box) => 
                        
                            output.tokens.forall({ (t: (Coll[Byte], Long)) =>
                                (t._1 != _CardTokenIssuerBox.id)
                            }) 
                        })

                    }

                    allOf(Coll(
                        validSelfRecreation,
                        validTokens,
                        validCardTokenBurn
                    ))

                }

                val validTradeInFeeBoxOUT: Boolean = if (OUTPUTS.size == 3) true else (OUTPUTS(1).propositionBytes == $TradeInFeeAddress)
                val validDevFeeBoxOUT: Boolean = if (OUTPUTS.size == 3) true else (OUTPUTS(2).propositionBytes == $DevAddress)

                allOf(Coll(
                    validCard,
                    validCardValueMappingBoxIN,
                    validGameLPBoxOUT,
                    validTradeInFeeBoxOUT,
                    validDevFeeBoxOUT
                ))

            }
            
        }

        sigmaProp(validTradeInTx)

    } else if (_TxType == 2) {

        // ===== Card-Value-Mapping Box Creation ===== //

        // Inputs
        val cardValueMappingIssuanceBoxIN: Box = INPUTS(1)
            
        // Outputs
        val gameLPBoxOUT: Box = OUTPUTS(0)
        val cardValueMappingBoxesOUT: Coll[Box] = OUTPUTS.slice(1, OUTPUTS.size-1)

        val isInitialCreation: Boolean = {

            allOf(Coll(
                (gameLPBoxOUT.tokens.slice(0, 2) == SELF.tokens.slice(0, 2)),
                (gameLPBoxOUT.tokens.size == 3)
            ))           
        
        }

        val validCardValueMappingBoxCreationTx: Boolean = {

            val validGameLPBoxOUT: Boolean = {

                val validSelfRecreation: Boolean = {

                    allOf(Coll(
                        (gameLPBoxOUT.value == SELF.value),
                        (gameLPBoxOUT.propositionBytes == SELF.propositionBytes),
                        (gameLPBoxOUT.R4[Coll[(Long, Long)]].get == SELF.R4[Coll[(Long, Long)]].get),
                        (gameLPBoxOUT.R5[Long].get == SELF.R5[Long].get),
                        (gameLPBoxOUT.R6[Long].get == SELF.R6[Long].get),
                        (gameLPBoxOUT.R7[Long].get == SELF.R7[Long].get),
                        (gameLPBoxOUT.R8[Long].get == SELF.R8[Long].get),
                        (gameLPBoxOUT.R9[Long].get == SELF.R9[Long].get)
                    ))

                }

                val validTokens: Boolean = {

                    allOf(Coll(
                        (gameLPBoxOUT.tokens.slice(0, SELF.tokens.size) == SELF.tokens),
                        (gameLPBoxOUT.tokens(SELF.tokens.size) == (cardValueMappingIssuanceBoxIN.tokens(0)._1, 1L)), // Adding the new card-value-mapping token to the game-lp box.
                        (gameLPBoxOUT.tokens.size == SELF.tokens.size + 1)
                    ))

                }

                allOf(Coll(
                    validSelfRecreation,
                    validTokens
                ))

            }

            val validCardValueMappingBoxesOUT: Boolean = {

                cardValueMappingBoxesOUT.forall({ (cardValueMappingBoxOUT: Box) =>

                    val validCardValueMappingBoxOUT: Boolean = {

                        allOf(Coll(
                            (cardValueMappingBoxOUT.propositionBytes == $CardValueMappingContractBytes),
                            (cardValueMappingBoxOUT.tokens(0) == (cardValueMappingIssuanceBoxIN.tokens(0)._1, 1L)),
                            (cardValueMappingBoxOUT.R4[Coll[Byte]].get == gameTokenId)
                        ))

                    }

                    allOf(Coll(
                        validCardValueMappingBoxOUT
                    ))

                })

            }

            allOf(Coll(
                validGameLPBoxOUT,
                validCardValueMappingBoxesOUT
            ))

        }

        sigmaProp(validCardValueMappingBoxCreationTx) && {

            if (isInitialCreation) {

                devPK


            } else {

                atLeast($SetCreationMultiSigThreshold, setCreationMultiSigAddresses)

            }

        } 

    } else if (_TxType == 3) {

        val validStorageRentTopUpTx: Boolean = {

            // Outputs
            val gameLPBoxOUT: Box = OUTPUTS(0)

            val validTopUp: Boolean = (gameLPBoxOUT.value > SELF.value)

            val validSelfRecreation: Boolean = {

                allOf(Coll(
                    (gameLPBoxOUT.tokens == SELF.tokens),
                    (gameLPBoxOUT.propositionBytes == SELF.propositionBytes),
                    (gameLPBoxOUT.R4[Coll[(Long, Long)]].get == SELF.R4[Coll[(Long, Long)]].get),
                    (gameLPBoxOUT.R5[Long].get == SELF.R5[Long].get),
                    (gameLPBoxOUT.R6[Long].get == SELF.R6[Long].get),
                    (gameLPBoxOUT.R7[Long].get == SELF.R7[Long].get),
                    (gameLPBoxOUT.R8[Long].get == SELF.R8[Long].get),
                    (gameLPBoxOUT.R9[Long].get == SELF.R9[Long].get)
                ))

            }

            allOf(Coll(
                validTopUp,
                validSelfRecreation
            ))

        }

        sigmaProp(validStorageRentTopUpTx) && devPK

    } else {
        sigmaProp(false)
    }
  
}