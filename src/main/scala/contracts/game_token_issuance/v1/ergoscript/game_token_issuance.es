{

    // ===== Contract Information ===== //
    // Name: Game Token Issuance
    // Description: This contract guards the Game Token Issuance box, which holds the minted game tokens.
    // Version: 1.0.0
    // Author: Luca D'Angelo (ldgaetano@protonmail.com)

    // ===== Box Contents ===== //
    // Tokens: Coll[(Coll[Byte], Long)]
    // 1. (GameTokenId, GameTokenAmount)
    // Registers:
    // R4: Coll[Byte] GameTokenName
    // R5: Coll[Byte] GameTokenDescription
    // R6: Coll[Byte] GameTokenDecimals

    // ===== Relevant Transactions ===== //
    // 1. Game LP Box Creation Tx
    // Inputs: GameLPIssuance, GameTokenIssuance
    // DataInputs: None
    // Outputs: GameLP, MinerFee
    // Context Variables: None
    
    // ===== Compile Time Constants ($) ===== //
    // $GameLPIssuanceContractBytes: Coll[Byte]
    // $GameLPContractBytes: Coll[Byte]
    // $SafeStorageRentValue: Long
    // $DevPK: SigmaProp
    // $DevAddress: Coll[Byte]
    // $MinerFee: Long

    // ===== Context Variables (_) ===== //
    // None

    // ===== Relevant Variables ===== //
    // None

    // ===== Game LP Box Creation Tx ===== //
    val validGameLPBoxCreationTx: Boolean = {

        // Inputs
        val gameLPIssuanceBoxIN: Box = INPUTS(0)
        
        // Outputs    
        val gameLPBoxOUT: Box = OUTPUTS(0)
        val minerFeeBoxOUT: Box = OUTPUTS(1)

        val validGameLPIssuanceBoxIN: Boolean = {

            val validValue: Boolean = (gameLPIssuanceBoxIN.value == $SafeStorageRentValue)
            val validContract: Boolean = ($GameLPIssuanceContractBytes == gameLPIssuanceBoxIN.propositionBytes)

            allOf(Coll(
                validValue,
                validContract
            ))

        }

        val validGameLPBoxOUT: Boolean = {

            val validValue: Boolean = ($SafeStorageRentValue == gameLPBoxOUT.value)
            val validContract: Boolean = ($GameLPContractBytes == gameLPBoxOUT.propositionBytes)
            val validTokens: Boolean = (gameLPBoxOUT.tokens(1) == (SELF.tokens(0)))
            val validRegister: Boolean = (gameLPBoxOUT.R4[Coll[Byte]].get == $DevAddress)

            allOf(Coll(
                validValue,
                validContract,
                validTokens,
                validRegister
            ))
            
        }

        val validMinerFee: Boolean = (minerFeeBoxOUT.value == $MinerFee)

        val validOutputSize: Boolean = (OUTPUTS.size == 2)

        allOf(Coll(
            validGameLPIssuanceBoxIN,
            validGameLPBoxOUT,
            validMinerFee,
            validOutputSize
        ))

    }

    sigmaProp(validGameLPBoxCreationTx) && $DevPK

}