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
    // 2. (GameTokenId, GameTokenAmount)
    // 3. (CardValueMappingToken, 1)
    // Registers:
    // R4: (Coll[(Long, Long)], Coll[Byte]) (Coll(DevFee, TradeInFee), GameTokenId)
    // R5: Long       EmissionInterval
    // R6: Long       EmissionReductionFactorMultiplier
    // R7: Long       EmissionReductionFactor
    // R8: Long       CardTokenBurnCount
    // R9: Long       CardTokenBurnTotal
    
    // ===== Relevant Transactions ===== //
    // 1. Trade In Tx
    // Inputs: GameLP, PlayerProxy
    // DataInputs: CardValueMapping
    // Outputs: GameLP, PlayerPK, DevAddress, TxOperator, MinerFee
    // Context Variables: None
    // 2. Card-Value-Mapping Box Creation Tx
    // Inputs: GameLP, CardValueMappingIssuance
    // DataInputs: None
    // Outputs: GameLP, CardValueMapping1, ... , CardValueMappingN, MinerFee
    // Context Variables: None
    
    // ===== Compile Time Constants ($) ===== //
    // $CardValueMappingContractBytes: Coll[Byte]
    // $DevPK: SigmaProp
    // $DevAddress: Coll[Byte]
    // $TradeInFeeAddress: SigmaProp
    // $MinBoxValue: Long
    // $SetCreationMultiSigThreshold: Int
    // $SetCreationMultiSigAddresses: Coll[SigmaProp]

    // ===== Context Variables (_) ===== //
    // _TransactionType: Byte
    // _CardSetCollectionIssuerBox: Box
    // _CardTokenIssuerBox: Box

    // ===== Transaction Types ===== //
    // 1 => Trade-In Tx
    // 2 => Card-Value-Mapping Box Creation Tx

    // ===== Relevant Variables ===== //
    val gameLPSingletonToken: (Coll[Byte], Long)    = SELF.tokens(0)
    val gameTokenId: Coll[Byte]                     = SELF.R4[(Coll[(Long, Long)], Coll[Byte])].get._2
    val emissionInterval: Long                      = SELF.R5[Long].get
    val emissionReductionFactorMultiplier: Long     = SELF.R6[Long].get
    val emissionReductionFactor: Long               = SELF.R7[Long].get
    val cardTokenBurnCount: Long                    = SELF.R8[Long].get
    val cardTokenBurnTotal: Long                    = SELF.R9[Long].get
    val _TxType: Byte                               = getVar[Byte](0).get
    val _CardSetCollectionIssuerBox: Box            = getVar[Box](1).get
    val _CardTokenIssuerBox: Box                    = getVar[Box](2).get
    val cardSetCollectionTokenId: Coll[Byte]        = _CardTokenIssuerBox.R7[Coll[Byte]].get

    if (_TxType == 1) {

        // ===== Trade-In Tx ===== //
        val validTradeInTx: Boolean = {

        // DataInputs
        val cardValueMappingBoxIN: Box                              = CONTEXT.dataInputs(0)
        val cardValueMappingGameTokenId: Coll[Byte]                 = cardValueMappingBoxIN.R4[Coll[Byte]].get
        val cardValueMappingCardSetCollectionTokenId: Coll[Byte]    = cardValueMappingBoxIN.R5[Coll[Byte]].get
        val cardValueMappingCardTokenId: Coll[Byte]                 = cardValueMappingBoxIN.R6[Coll[Byte]].get
        val cardValueMappingCardValue: Long                         = cardValueMappingBoxIN.R7[Long].get

        // Outputs
        val gameLPBoxOUT: Box       = OUTPUTS(0)
        val tradeInFeeBoxOUT: Box   = OUTPUTS(1)
        val devAddressBoxOUT: Box   = OUTPUTS(2)

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

            val cardValueMappingToken: (Coll[Byte], 1L) = cardValueMappingBoxIN.tokens(0)

            // Conditions:
            // 1. We have a legit card value mapping box with the correct contract
            // 2. We have a legit card value mapping token
            // 3. The card value mapping box is associated to a legit card set
            // 4. The card value mapping box is assocaited to a legit card
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

            val validValue: Boolean = (gameLPBoxOUT.value == SELF.value)
            val validContract: Boolean = (gameLPBoxOUT.propositionBytes == SELF.propositionBytes)
          
            val validTokens: Boolean = {
                
                if (SELF.tokens(1)._1 == gameTokenId) { // Not all game tokens have been distributed.

                    val validGameLPSingletonToken: Boolean = (gameLPBoxOUT.tokens(0) == (gameLPSingletonToken._1, 1L))
                    
                    val validGameTokens: Boolean = {

                        if (gameLPBoxOUT.tokens.size == SELF.tokens.size) { // If nothing about the amount of different tokens changes

                            allOf(Coll(
                                (gameLPBoxOUT.tokens(1)._1 == SELF.tokens(1)._1),
                                (gameLPBoxOUT.tokens.slice(2, gameLPBoxOUT.tokens.size) == SELF.tokens.slice(2, SELF.tokens.size))
                            ))

                        
                        }  else { // Otherwise, the amount of different tokens must decrease and there must therefore be no more game tokens.

                            allOf(Coll(
                                (gameLPBoxOUT.tokens.size < SELF.tokens.size),
                                (gameLPBoxOUT.tokens.slice(1, gameLPBoxOUT.tokens.size) == SELF.tokens.slice(2, SELF.tokens.size))
                            ))

                        } 

                    }

                    allOf(Coll(
                        validGameLPSingletonToken,
                        validGameTokens
                    ))


                } else { // All game tokens have been distributed, no more to give out by burning card tokens.
                    false
                }

            }

            val validRegisters: Boolean = {

                allOf(Coll(
                    (gameLPBoxOUT.R4[(Coll[(Long, Long)], Coll[Byte])].get == SELF.R4[(Coll[(Long, Long)], Coll[Byte])].get),
                    (gameLPBoxOUT.R5[Long].get == emissionInterval),
                    (gameLPBoxOUT.R6[Long].get == emissionReductionFactorMultiplier)
                ))

            }

            val validBurn: Boolean = {



            }

            allOf(Coll(
                validValue,
                validContract,
                validTokens,
                validRegisters
            ))

        }

        val validPlayerPKBoxOUT: Boolean = {

            // Only check that player pk box has a game token in it, but the exact value is determined by the player proxy contract.
            val validGameTokenId: Boolean = (playerPKBoxOUT.tokens(0)._1 == SELF.tokens(1)._1)

            allOf(Coll(
                validGameTokenId
            ))

        }

        val validDevAddressBoxOUT: Boolean = {

            val validContract: Boolean = (devAddressBoxOUT.propositionBytes == $DevAddress)
            val validGameToken: Boolean = (devAddressBoxOUT.tokens(0)._1 == SELF.tokens(1)._1) // Only check that the dev box has a game token in it, but the exact value is determined by the player proxy contract.

            allOf(Coll(
                validValue,
                validContract,
                validGameToken
            ))

        }

        // val validTxOperatorBoxOUT: Boolean = {

        //     val validValue: Boolean = (txOperatorBoxOUT.value == $MinBoxValue)
        //     val validGameToken: Boolean = (txOperatorBoxOUT.tokens(0)._1 == SELF.tokens(1)._1) // Only check that the tx operator box has a game token in it, but the exact value is determined by the player proxy contract.

        //     allOf(Coll(
        //         validValue,
        //         validGameToken
        //     ))

        // }

        allOf(Coll(
            validPlayerProxyBoxIN,
            validCardValueMappingBoxIN,
            validGameLPBoxOUT,
            validPlayerPKBoxOUT,
            validDevAddressBoxOUT
        ))
        
        }

        sigmaProp(validTradeInTx)

    } else if (txType == 2) {

        // ===== Card-Value-Mapping Box Creation ===== //
        val validCardValueMappingBoxCreationTx: Boolean = {

            // Inputs
            val cardValueMappingIssuanceBoxIN: Box = INPUTS(1)
            
            // Outputs
            val gameLPBoxOUT: Box = OUTPUTS(0)
            val cardValueMappingBoxesOUT: Coll[Box] = OUTPUTS.slice(1, OUTPUTS.size-1)
            val minerFeeBoxOUT: Box = OUTPUTS(OUTPUTS.size-1)

            val isInitialCreation: Boolean = {

                // 1. The lp output box has three tokens, the third being the first card-value mapping token id.
                // 2. The lp output box must contain the game token id.

                allOf(Coll(
                    (gameLPBoxOUT.tokens.size == 3),
                    (gameLPBoxOUT.tokens.exists({ (t: (Coll[Byte], Long) => t._1 == gameTokenId) }))
                ))           
            
            }

            val validGameLPBoxOUT: Boolean = {

                val validSelfRecreation: Boolean = {

                    allOf(Coll(
                        (gameLPBoxOUT.value == SELF.value),
                        (gameLPBoxOUT.propositionBytes == SELF.propositionBytes),
                        (gameLPBoxOUT.R4[(Coll[(Long, Long)], Coll[Byte])].get == SELF.R4[(Coll[(Long, Long)], Coll[Byte])].get),
                        (gameLPBoxOUT.R5[Long].get == SELF.R5[Long].get),
                        (gameLPBoxOUT.R6[Long].get == SELF.R6[Long].get),
                        (gameLPBoxOUT.R7[Long].get == SELF.R7[Long].get),
                        (gameLPBoxOUT.R8[Long].get == SELF.R8[Long].get),
                        (gameLPBoxOUT.R9[Long].get == SELF.R9[Long].get)
                    ))

                }

                val validTokens: Boolean = {

                    if (SELF.tokens(1)._1 == gameTokenId) { // Not all game tokens have been distributed.

                        allOf(Coll(
                            (gameLPBoxOUT.tokens(0) == SELF.tokens(0)),
                            (gameLPBoxOUT.tokens(1) == SELF.tokens(1)),
                            (gameLPBoxOUT.tokens(gameLPBoxOUT.tokens.size-1) == (cardValueMappingIssuanceBoxIN.tokens(0)._1, 1L)) // Adding the new card-value-mapping token to the game-lp box.
                            (gameLPBoxOUT.tokens.size == SELF.tokens.size + 1)
                        ))

                    } else {
                        false
                    }

                }

                allOf(Coll(
                    validSelfRecreation,
                    validTokens
                ))

            }

            allOf(Coll(
                validGameLPBoxOUT
            ))
        }

        sigmaProp(validCardValueMappingBoxCreationTx) && {

            if (isInitialCreation) {

                $DevPK


            } else {

                atLeast($SetCreationMultiSigThreshold, $SetCreationMultiSigAddresses)

            }

        } 

    } else {
        sigmaProp(false)
    }
  
}