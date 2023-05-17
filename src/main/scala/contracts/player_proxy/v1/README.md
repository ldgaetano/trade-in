# trade-in: Player Proxy Contract - v1.0.0

## Contract

- [ErgoScript](ergoscript/player_proxy.es)

## Documentation

### Description
This contract guards the Player Proxy box, which holds the player's card token. The logic of this contract determines how many game tokens the player will receive based on the card they hold.

### Box Contents
Tokens: Coll[(Coll[Byte], Long)]
1. (CardTokenId, 1)

Registers:
- R4: SigmaProp  PlayerPK
- R5: Coll[Byte] GameLPSingletonTokenId
- R6: Coll[Byte] GameTokenId
- R7: Coll[Byte] CardValueMappingSingletonTokenId

### Relevant Transactions
1. Trade-In Tx
- Inputs: GameLP, PlayerProxy
- DataInputs: CardValueMapping
- Outputs: GameLP, PlayerPK, DevAddress, TxOperator, MinerFee
- Context Variables: CardTokenIssuerBox, CardSetCollectionIssuerBox

### Compile Time Constants ($)
- $MinerFee

### Context Variables (#)
- #CardTokenIssuerBox: Box
- #CardSetCollectionIssuerBox: Box

### User Defined Functions
- def selectRegister(bucket: Byte): Coll[(Coll[Byte], Long)]