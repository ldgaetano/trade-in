# trade-in
Card game token burn and trade-in protocol for Ergo blockchain.

### KYA

1. Use at your own risk.

### Installing

1. Download the latest release to minimize your own risk, or clone/download repository if you are adventurous.
2. Install Java 18.0.2 (JRE, JDK, or OpenJDK).
3. If you would like to compile the jar yourself, download `sbt` and run `sbt assembly` within the repository folder.

### Configurations

#### Setup Configuration

###### Node Configuration
###### Settings Configuration

#### Report Configuration

### Usage

#### Trade-In Setup

This command will compile all the ErgoScript contracts and execute all the transactions in the setup protocol.

1. Run `java -jar trade-in-<version>.jar --trade-in-setup`

#### Contract Compilation

This command will compile all the ErgoScript contracts required for the protocol.

1. Run `java -jar trade-in-<version>.jar --compile`

#### Transaction Execution

This command will execute all transactions in the setup phase of the protocol. It assumes that you have compiled the ErgoScript contracts already.

1. Run `java -jar trade-in-<version>.jar --all` to execute all transactions in the protocol.
2. Run `java -jar trade-in-<version>.jar --mint-game-tokens` to execute the game token mint transaction.
3. Run `java -jar trade-in-<version>.jar --mint-game-lp-singleton` to execute the game liquidity pool singleton token minting transaction.
4. Run `java -jar trade-in-<version>.jar --game-lp-creation` to execute the game lp box creation transaction.
5. Run `java -jar trade-in-<version>.jar --mint-card-value-mapping-singleton` to execute the card-value-mapping singleton token minting transaction.
6. Run `java -jar trade-in-<version>.jar --card-value-mapping-creation` to execute the card-value-mapping box creation transaction.

### Protocol Assumptions

The Player Proxy transaction is not part of this setup protocol but will instead be executed by the bot. However, this will assume that a valid game token already exists.

### Reporting Issues

Please create a GitHub issue or email me.