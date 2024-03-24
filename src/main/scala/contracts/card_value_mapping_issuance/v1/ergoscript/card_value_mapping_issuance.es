{

    // ===== Contract Information ===== //
    // Name: Card Value Mapping Issuance
    // Description: This contract guards the Card Value Mapping Issuance box, which holds the 
    //              minted singleton token to uniquely identify the Card Value Mapping box.
    // Version: 1.0.0
    // Author: Luca D'Angelo (ldgaetano@protonmail.com)

    // ===== Box Contents ===== //
    // Tokens: Coll[(Coll[Byte], Long)]
    // 1. (CardValueMappingTokenId, CardSetSize + 1)
    // Registers:
    // R4: Coll[Byte] CardValueMappingTokenName
    // R5: Coll[Byte] CardValueMappingTokenDescription
    // R6: Coll[Byte] CardValueMappingTokenDecimals

    // ===== Relevant Transactions ===== //
    // 1. Card Value Mapping Box Creation Tx
    // Inputs: GameLP, CardValueMappingIssuance
    // DataInputs: None
    // Outputs: GameLP, CardValueMapping1, ... , CardValueMappingBoxN, MinerFee
    // Context Variables: None
    
    // ===== Compile Time Constants ($) ===== //
    // $GameLPContractBytes: Coll[Byte]
    // $CardValueMappingContractBytes: Coll[Byte]
    // $CardSetSize: Long
    // $SafeStorageRentValue: Long
    // $DevPK: GroupElement
    // $MinerFee: Long

    // ===== Context Variables (_) ===== //
    // None

    // ===== Relevant Variables ===== //
    val devPK: SigmaProp = proveDlog($DevPK)

    // ===== Card Value Mapping Box Creation Tx ===== //
    val validCardValueMappingBoxCreationTx: Boolean = {

        // Inputs
        val gameLPBoxIN: Box = INPUTS(0)
        
        // Outputs    
        val gameLPBoxOUT: Box = OUTPUTS(0)
        val cardValueMappingBoxesOUT: Coll[Box] = OUTPUTS.slice(1, OUTPUTS.size-1)
        val minerFeeBoxOUT: Box = OUTPUTS(OUTPUTS.size-1)

        val validGameLPBoxIN: Boolean = (gameLPBoxIN.propositionBytes == $GameLPContractBytes)

        val validCardValueMappingBoxesOUT: Boolean = cardValueMappingBoxesOUT.forall({ (cardValueMappingBoxOUT: Box) =>
        
            val validValue: Boolean = ($SafeStorageRentValue == cardValueMappingBoxOUT.value)
            val validContract: Boolean = ($CardValueMappingContractBytes == cardValueMappingBoxOUT.propositionBytes)
            val validTokens: Boolean = (cardValueMappingBoxOUT.tokens(0) == (SELF.tokens(0)._1, 1L))

            allOf(Coll(
                validValue,
                validContract,
                validTokens
            ))
        
        })

        val validMinerFee: Boolean = (minerFeeBoxOUT.value == $MinerFee)

        val validOutputSize: Boolean = (OUTPUTS.size == ($CardSetSize + 1L).toInt)

        allOf(Coll(
            validGameLPBoxIN,
            validCardValueMappingBoxOUT,
            validMinerFee,
            validOutputSize
        ))

    }

    sigmaProp(validCardValueMappingBoxCreationTx) && devPK

}