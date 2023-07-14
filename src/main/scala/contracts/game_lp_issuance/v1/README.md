# Trade-In: Game LP Issuance Contract - v1.0.0

## Contract

- [ErgoScript](ergoscript/game_lp_issuance.es)

## Documentation

### Description
This contract guards the Game LP Issuance box, which holds the minted LP box singleton token. This singleton token will be used as the unique identifier of the Game LP box. This is required because as the LP box is recreated during withdrawls, the box id will change.

### Box Contents
Tokens: Coll[(Coll[Byte], Long)]
1. (GameLPSingletonToken, 1)

Registers:
- R4: Coll[Byte] GameLPSingletonTokenName
- R5: Coll[Byte] GameLPSingletonTokenDescription
- R6: Coll[Byte] GameLPSingletonTokenDecimals

### Relevant Transactions
1. Game LP Box Creation Tx
- Inputs: GameLPIssuance, GameTokenIssuance
- DataInputs: None
- Outputs: GameLP, MinerFee
- Context Variables: None

### Compile Time Constants ($)
- $GameLPContractBytes: Coll[Byte]
- $SafeStorageRentValue: Long
- $DevPK: SigmaProp
- $DevAddress: Coll[Byte]
- $MinerFee: Long

### Context Variables (_)
- None