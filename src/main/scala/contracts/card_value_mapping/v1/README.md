# trade-in: Card Value Mapping Contract - v1.0.0

## Contract

- [ErgoScript](ergoscript/card_value_mapping.es)

## Documentation

### Description
This contract guards the Card Value Mapping box, which holds in its registers a map from the hash of the card token id to its associated maximum possible value in denominated in the appropriate game token. This box will be used as a data input into the trade-in transaction so that it can be referenced by multiple transactions at the same time.

### Box Contents
Tokens: Coll[(Coll[Byte], Long)]
1. (CardValueMappingSingletonTokenId, 1)

Registers:
- R4: (Coll[Byte], (Coll[Byte], (Long, (Byte, Byte)))) (CardSetCollectionTokenId, (GameTokenId, (TotalCards, (TotalDataBuckets, DataBucketSize))))
- R5: Coll[(Coll[Byte], Long)] Coll[(CardTokenHash, MaxCardValue)] => DataBucket0
- R6: Coll[(Coll[Byte], Long)] Coll[(CardTokenHash, MaxCardValue)] => DataBucket1
- R7: Coll[(Coll[Byte], Long)] Coll[(CardTokenHash, MaxCardValue)] => DataBucket2
- R8: Coll[(Coll[Byte], Long)] Coll[(CardTokenHash, MaxCardValue)] => DataBucket3
- R9: Coll[(Coll[Byte], Long)] Coll[(CardTokenHash, MaxCardValue)] => DataBucket4

### Relevant Transactions
1. Trade-In Tx
- Inputs: GameLP, PlayerProxy
- DataInputs: CardValueMapping
- Outputs: GameLP, PlayerPK, DevAddress, TxOperator, MinerFee
- Context Variables: None

### Compile Time Constants ($)
- None

### Context Variables (#)
- None