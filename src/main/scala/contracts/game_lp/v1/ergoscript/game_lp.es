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
    // Registers:
    // R4: Coll[Byte] DevAddress
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
    
    // ===== Compile Time Constants ($) ===== //
    // $CardValueMappingContractBytes: Coll[Byte]
    // $PlayerProxyContractBytes: Coll[Byte]
    // $DevFee: Long
    // $TxOperatorFee: Long
    // $MinBoxValue: Long

    // ===== Context Variables (#) ===== //
    // None

    // ===== Relevant Variables ===== //
    val gameLPSingletonToken: (Coll[Byte], Long) = SELF.tokens(0)
    val gameTokens: (Coll[Byte], Long) = SELF.tokens(1)
    val devAddress: Coll[Byte] = SELF.R4[Coll[Byte]].get
    val emissionInterval: Long = SELF.R5[Long].get
    val emissionReductionFactorMultiplier: Long = SELF.R6[Long].get

    // ===== Trade-In Tx ===== //
    val validTradeInTx: Boolean = {

        // Inputs
        val playerProxyBoxIN: Box = INPUTS(1)

        // DataInputs
        val cardValueMappingBoxIN: Box = CONTEXT.dataInputs(0)
        val cardValueMappingBoxData: (Coll[Byte], (Coll[Byte], (Long, (Byte, Byte)))) = cardValueMappingBoxIN.R4[(Coll[Byte], (Coll[Byte], (Long, (Byte, Byte))))].get


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

            val cardValueMappingGameTokenId: Coll[Byte] = cardValueMappingBoxData._2._1

            val validContract: Boolean = (cardValueMappingBoxIN.propositionBytes == $CardValueMappingContractBytes)
            val validGameTokenId: Boolean = if (SELF.tokens.size == 2) (cardValueMappingGameTokenId == gameTokens._1) else false

            allOf(Coll(
                validContract,
                validGameTokenId
            ))

        }

        val validGameLPBoxOUT: Boolean = {

            val validValue: Boolean = (gameLPBoxOUT.value == SELF.value)
            val validContract: Boolean = (gameLPBoxOUT.propositionBytes == SELF.propositionBytes)
            
            val validTokens: Boolean = {

                val validGameLPSingletonToken: Boolean = (gameLPBoxOUT.tokens(0) == (gameLPSingletonToken._1, 1L))
                val validGameTokens: Boolean = {

                    val validTokenId: Boolean = (gameLPBoxOUT.tokens(1)._1 == gameTokens._1)
                    val validTokenAmount: Boolean = if (gameLPBoxOUT.tokens.size == 2) (gameLPBoxOUT.tokens(1)._2 == gameTokens._2 - playerPKBoxOUT.tokens(0)._2 - devAddressBoxOUT.tokens(0)._2 - txOperatorBoxOUT.tokens(0)._2) else (gameLPBoxOUT.tokens.size == 1) // Cannot deposit to the Game LP box, can only withdraw from it.    

                    allOf(Coll(
                        validTokenId,
                        validTokenAmount
                    ))

                }

                allOf(Coll(
                    validGameLPSingletonToken,
                    validGameTokens
                ))

            }

            val validRegisters: Boolean = {

                // The remaining registers will be determined by the Player Proxy contract
                allOf(Coll(
                    (gameLPBoxOUT.R4[Coll[Byte]].get == devAddress),
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
            val validTokenId: Boolean = (playerPKBoxOUT.tokens(0)._1 == gameLPSingletonToken._1)

            allOf(Coll(
                validTokenId
            ))

        }

        val validDevAddressBoxOUT: Boolean = {

            val validValue: Boolean = (devAddressBoxOUT.value == $MinBoxValue)
            val validContract: Boolean = (devAddressBoxOUT.propositionBytes == devAddress)
            val validToken: Boolean = (devAddressBoxOUT.tokens(0) == (gameTokens._1, $DevFee))

            allOf(Coll(
                validValue,
                validContract,
                validToken
            ))

        }

        val validTxOperatorBoxOUT: Boolean = {

            val validValue: Boolean = (txOperatorBoxOUT.value == $MinBoxValue)
            val validToken: Boolean = (txOperatorBoxOUT.tokens(0) == (gameTokens._1, $TxOperatorFee))

            allOf(Coll(
                validValue,
                validToken
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

}