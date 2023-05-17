{

    // ===== Contract Information ===== //
    // Name: Card Value Mapping
    // Description: This contract guargs the Card Value Mapping box, which holds in its registers a map
    //              from the hash of the card token id to its associated maximum possible value demoninated
    //              in the appropriate game token. This box will be used as a data input into the trade-in
    //              transaction so that it can be referenced by multiple transactions at the same time.
    // Version: 1.0.0
    // Author: Luca D'Angelo (ldgaetano@protonmail.com)

    // ===== Box Contents ===== //
    // Tokens: Coll[(Coll[Byte], Long)]
    // 1. (CardValueMappingSingletonTokenId, 1)
    // Registers:
    // R4: (Coll[Byte], (Coll[Byte], (Long, (Byte, Byte)))) (CardSetCollectionTokenId, (GameTokenId, (TotalCards, (TotalDataBuckets, DataBucketSize))))
    // R5: Coll[(Coll[Byte], Long)] Coll[(CardTokenHash, MaxCardValue)] => DataBucket0
    // R6: Coll[(Coll[Byte], Long)] Coll[(CardTokenHash, MaxCardValue)] => DataBucket1
    // R7: Coll[(Coll[Byte], Long)] Coll[(CardTokenHash, MaxCardValue)] => DataBucket2
    // R8: Coll[(Coll[Byte], Long)] Coll[(CardTokenHash, MaxCardValue)] => DataBucket3
    // R9: Coll[(Coll[Byte], Long)] Coll[(CardTokenHash, MaxCardValue)] => DataBucket4

    // ===== Relevant Transactions ===== //
    // 1. Trade-In Tx
    // Inputs: GameLP, PlayerProxy
    // DataInputs: CardValueMapping
    // Outputs: GameLP, PlayerPK, DevAddress, TxOperator, MinerFee
    // Context Variables: None
    
    // ===== Compile Time Constants ($) ===== //
    // None

    // ===== Context Variables (#) ===== //
    // None

    // ===== Relevant Variables ===== //
    // None

    // ===== Trade-In Tx ===== //
    sigmaProp(false) // This box cannot be spent.

}