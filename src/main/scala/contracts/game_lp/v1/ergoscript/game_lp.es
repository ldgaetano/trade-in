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
    // R4: (Coll[(Long, Long)], (Coll[Byte], Coll[Byte])) (Coll(DevFee, TxOperatorFee), (DevAddress, GameTokenId))
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
    // $PlayerProxyContractBytes: Coll[Byte]
    // $DevPK: SigmaProp
    // $MinBoxValue: Long

    // ===== Context Variables (_) ===== //
    // _TransactionType: Byte

    // ===== Relevant Variables ===== //
    val gameLPSingletonToken: (Coll[Byte], Long) = SELF.tokens(0)
    val devAddress: Coll[Byte] = SELF.R4[(Coll[(Long, Long)], (Coll[Byte], Coll[Byte]))].get._2._1
    val gameTokenId: Coll[Byte] = SELF.R4[(Coll[(Long, Long)], (Coll[Byte], Coll[Byte]))].get._2._2
    val emissionInterval: Long = SELF.R5[Long].get
    val emissionReductionFactorMultiplier: Long = SELF.R6[Long].get
    val txType: Byte = getVar[Byte](0).get

    if (txType == 1) {

        // ===== Trade-In Tx ===== //
        val validTradeInTx: Boolean = {

        // Inputs
        val playerProxyBoxIN: Box = INPUTS(1)

        // DataInputs
        val cardValueMappingBoxIN: Box = CONTEXT.dataInputs(0)

        // Outputs
        val gameLPBoxOUT: Box = OUTPUTS(0)
        val playerPKBoxOUT: Box = OUTPUTS(1)
        val devAddressBoxOUT: Box = OUTPUTS(2)
        val txOperatorBoxOUT: Box = OUTPUTS(3)
        val minerFeeBoxOUT: Box = OUTPUTS(4)

        val validPlayerProxyBoxIN: Boolean = {

            val validContract: Boolean = (playerProxyBoxIN.propositionBytes == $PlayerProxyContractBytes)

            allOf(Coll(
                validContract
            ))

        }

        val validCardValueMappingBoxIN: Boolean = {

            val cardValueMappingToken: (Coll[Byte], 1L) = cardValueMappingBoxIN.tokens(0)
            val cardValueMappingGameTokenId: Coll[Byte] = cardValueMappingBoxIN.R4[Coll[Byte]].get

            val validContract: Boolean = (cardValueMappingBoxIN.propositionBytes == $CardValueMappingContractBytes)
            val validCardValueMappingToken: Boolean = SELF.tokens.exists({ (t: (Coll[Byte], Long)) => (t == cardValueMappingToken) })
            val validGameTokenId: Boolean = (cardValueMappingGameTokenId == gameTokenId)

            allOf(Coll(
                validContract,
                validCardValueMappingToken
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

                            (gameLPBoxOUT.tokens(1)._1 == SELF.tokens(1)._1)
                        
                        }  else { // Otherwise, the amount of different tokens must decrease and there must therefore be no more game tokens.

                            allOf(Coll(
                                (gameLPBoxOUT.tokens.size < SELF.tokens.size),
                                (gameLPBoxOUT.tokens(1)._1 != gameTokenId)
                            ))

                        } 

                    }

                    val validCardValueMappingTokens: Boolean = {

                        if (gameLPBoxOUT.tokens.size == SELF.tokens.size) { // There position must not change.

                            (gameLPBoxOUT.tokens.slice(2, gameLPBoxOUT.tokens.size) == SELF.tokens.slice(2, SELF.tokens.size))

                        } else { // There position should change but the tokens remain the same.

                            (gameLPBoxOUT.tokens.slice(1, gameLPBoxOUT.tokens.size) == SELF.tokens.slice(2, SELF.tokens.size))

                        }

                    }

                    allOf(Coll(
                        validGameLPSingletonToken,
                        validGameTokens,
                        validCardValueMappingTokens
                    ))


                } else { // All game tokens have been distributed, no more to give out by burning card tokens.
                    false
                }

            }

            val validRegisters: Boolean = {

                // The remaining registers will be determined by the Player Proxy contract
                allOf(Coll(
                    (gameLPBoxOUT.R4[(Coll[(Long, Long)], (Coll[Byte], Coll[Byte]))].get == SELF.R4[(Coll[(Long, Long)], (Coll[Byte], Coll[Byte]))].get),
                    (gameLPBoxOUT.R5[Long].get == emissionInterval),
                    (gameLPBoxOUT.R6[Long].get == emissionReductionFactorMultiplier)
                ))

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

            val validValue: Boolean = (devAddressBoxOUT.value == $MinBoxValue)
            val validContract: Boolean = (devAddressBoxOUT.propositionBytes == devAddress)
            val validGameToken: Boolean = (devAddressBoxOUT.tokens(0)._1 == SELF.tokens(1)._1) // Only check that the dev box has a game token in it, but the exact value is determined by the player proxy contract.

            allOf(Coll(
                validValue,
                validContract,
                validGameToken
            ))

        }

        val validTxOperatorBoxOUT: Boolean = {

            val validValue: Boolean = (txOperatorBoxOUT.value == $MinBoxValue)
            val validGameToken: Boolean = (txOperatorBoxOUT.tokens(0)._1 == SELF.tokens(1)._1) // Only check that the tx operator box has a game token in it, but the exact value is determined by the player proxy contract.

            allOf(Coll(
                validValue,
                validGameToken
            ))

        }

        val validOutputSize: Boolean = (OUTPUTS.size == 5)

        allOf(Coll(
            validPlayerProxyBoxIN,
            validCardValueMappingBoxIN,
            validGameLPBoxOUT,
            validPlayerPKBoxOUT,
            validDevAddressBoxOUT,
            validTxOperatorBoxOUT,
            validOutputSize
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


            val validGameLPBoxOUT: Boolean = {

                val validSelfRecreation: Boolean = {

                    allOf(Coll(
                        (gameLPBoxOUT.value == SELF.value),
                        (gameLPBoxOUT.propositionBytes == SELF.propositionBytes),
                        (gameLPBoxOUT.R4[(Coll[(Long, Long)], (Coll[Byte], Coll[Byte]))].get == SELF.R4[(Coll[(Long, Long)], (Coll[Byte], Coll[Byte]))].get),
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

        sigmaProp(validCardValueMappingBoxCreationTx) && $DevPK

    } else {
        sigmaProp(false)
    }
  
}