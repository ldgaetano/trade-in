{

    // ===== Contract Information ===== //
    // Name: Card Value Mapping Issuance
    // Description: This contract guards the Card Value Mapping Issuance box, which holds the 
    //              minted singleton token to uniquely identify the Card Value Mapping box.
    // Version: 1.0.0
    // Author: Luca D'Angelo (ldgaetano@protonmail.com)

    // ===== Box Contents ===== //
    // Tokens: Coll[(Coll[Byte], Long)]
    // 1. (CardValueMappingSingletonTokenId, 1)
    // Registers:
    // R4: Coll[Byte] CardValueMappingSingletonTokenName
    // R5: Coll[Byte] CardValueMappingSingletonTokenDescription
    // R6: Coll[Byte] CardValueMappingSingletonTokenDecimals

    // ===== Relevant Transactions ===== //
    // 1. Card Value Mapping Box Creation Tx
    // Inputs: CardValueMappingIssuance
    // DataInputs: None
    // Outputs: CardValueMapping, MinerFee
    // Context Variables: None
    
    // ===== Compile Time Constants ($) ===== //
    // $CardValueMappingContractBytes: Coll[Byte]
    // $SafeStorageRentValue: Long
    // $DevPK: SigmaProp
    // $MinerFee: Long

    // ===== Context Variables (#) ===== //
    // None

    // ===== Relevant Variables ===== //
    // None

    // ===== Card Value Mapping Box Creation Tx ===== //
    val validCardValueMappingBoxCreationTx: Boolean = {
        
        // Outputs    
        val cardValueMappingBoxOUT: Box = OUTPUTS(0)
        val minerFeeBoxOUT: Box = OUTPUTS(1)

        val validCardValueMappingBoxOUT: Boolean = {

            val validValue: Boolean = ($SafeStorageRentValue == cardValueMappingBoxOUT.value)
            val validContract: Boolean = ($CardValueMappingContractBytes == cardValueMappingBoxOUT.propositionBytes)
            val validSingletonToken: Boolean = ((cardValueMappingBoxOUT.tokens(0)._1, 1L) == SELF.tokens(0))

            allOf(Coll(
                validValue,
                validContract,
                validSingletonToken
            ))
            
        }

        val validMinerFee: Boolean = (minerFeeBoxOUT.value == $MinerFee)

        val validOutputSize: Boolean = (OUTPUTS.size == 2)

        allOf(Coll(
            validCardValueMappingBoxOUT,
            validMinerFee,
            validOutputSize
        ))

    }

    sigmaProp(validCardValueMappingBoxCreationTx) && $DevPK

}