package utils

import builders.contract_builders.{CardValueMappingContractBuilder, CardValueMappingIssuanceContractBuilder, GameLPContractBuilder, GameLPIssuanceContractBuilder, GameTokenIssuanceContractBuilder, PlayerProxyContractBuilder}
import builders.transaction_builders.{CardValueMappingSingletonTokenMintingTxBuilder, GameLPBoxCreationTxBuilder, GameLPSingletonTokenMintingTxBuilder, GameTokenMintingTxBuilder}
import configs.report_config.TradeInReportConfig
import configs.setup_config.TradeInSetupConfig
import org.ergoplatform.appkit.{Address, BlockchainContext, ErgoContract, ErgoProver, NetworkType, OutBox, SignedTransaction, UnsignedTransaction}

import java.nio.file.{Files, Paths}
import java.time.{LocalDateTime, ZoneId}
import scala.util.Try

object TradeInUtils {

  // CLI logo
  final val tradeInLogo: String =
    """
      |
      |'########'########::::'###:::'########:'########::::::::'####'##::: ##:
      |... ##..::##.... ##::'## ##:::##.... ##:##.....:::::::::. ##::###:: ##:
      |::: ##::::##:::: ##:'##:. ##::##:::: ##:##::::::::::::::: ##::####: ##:
      |::: ##::::########:'##:::. ##:##:::: ##:######::'#######: ##::## ## ##:
      |::: ##::::##.. ##:::#########:##:::: ##:##...:::........: ##::##. ####:
      |::: ##::::##::. ##::##.... ##:##:::: ##:##::::::::::::::: ##::##:. ###:
      |::: ##::::##:::. ##:##:::: ##:########::########::::::::'####:##::. ##:
      |:::..::::..:::::..:..:::::..:........::........:::::::::....:..::::..::
      |
      |""".stripMargin

  // File Paths
  final val TRADEIN_SETUP_CONFIG_FILE_PATH: String = "settings/tradein_setup_config.json"
  final val TRADEIN_REPORT_CONFIG_FILE_PATH: String = "settings/tradein_report_config.json"
  final val TRADEIN_GAME_TOKEN_IMG_DIRECTORY_PATH: String = "img/"

  // ErgoScript contracts
  final val GAME_TOKEN_ISSUANCE_SCRIPT: String = Files.readString(Paths.get("src/main/scala/contracts/game_token_issuance/v1/ergoscript/game_token_issuance.es")).stripMargin
  final val GAME_LP_ISSUANCE_SCRIPT: String = Files.readString(Paths.get("src/main/scala/contracts/game_lp_issuance/v1/ergoscript/game_lp_issuance.es")).stripMargin
  final val GAME_LP_SCRIPT: String = Files.readString(Paths.get("src/main/scala/contracts/game_lp/v1/ergoscript/game_lp.es")).stripMargin
  final val CARD_VALUE_MAPPING_ISSUANCE_SCRIPT: String = Files.readString(Paths.get("src/main/scala/contracts/card_value_mapping_issuance/v1/ergoscript/card_value_mapping_issuance.es")).stripMargin
  final val CARD_VALUE_MAPPING_SCRIPT: String = Files.readString(Paths.get("src/main/scala/contracts/card_value_mapping/v1/ergoscript/card_value_mapping.es")).stripMargin
  final val PLAYER_PROXY_SCRIPT: String = Files.readString(Paths.get("src/main/scala/contracts/player_proxy/v1/ergoscript/player_proxy.es")).stripMargin

  // Box Values: nanoERGs
  final val STORAGE_RENT_FEE_PERIOD_IN_YEARS: Long = 4L
  final val STORAGE_RENT_FEE_PER_BYTE_PER_PERIOD_IN_YEARS: Long = 1250000L
  final val STORAGE_RENT_FEE_PER_BYTE_PER_YEAR: Long = STORAGE_RENT_FEE_PER_BYTE_PER_PERIOD_IN_YEARS / STORAGE_RENT_FEE_PERIOD_IN_YEARS
  final val MIN_BOX_VALUE_PER_BYTE: Long = 360L

  def calcMinBoxValue(): Long = {

    // For simplicity, just assume the box has the max size, conservative fee value
    val maxBoxSize: Int = 4096
    MIN_BOX_VALUE_PER_BYTE * maxBoxSize

  }

  def calcSafeStorageRentValue(lifetime: Long): Long = {

    // we just assume max box size for simplicity and to be conservative
    val maxBoxSize: Int = 4096

    if (lifetime < STORAGE_RENT_FEE_PERIOD_IN_YEARS) {

      MIN_BOX_VALUE_PER_BYTE * maxBoxSize

    } else {

      val txFee: Long = MIN_BOX_VALUE_PER_BYTE / STORAGE_RENT_FEE_PERIOD_IN_YEARS

      (STORAGE_RENT_FEE_PER_BYTE_PER_YEAR + txFee) * lifetime * maxBoxSize

    }

  }

  // Network Info
  final val ERGO_EXPLORER_TX_URL_PREFIX_MAINNET: String = "https://explorer.ergoplatform.com/en/transactions/"
  final val ERGO_EXPLORER_TX_URL_PREFIX_TESTNET: String = "https://testnet.ergoplatform.com/en/transactions/"

  // Node Info
  final val DEFAULT_LOCAL_NODE_MAINNET_API_URL: String = "http://127.0.0.1:9053/"
  final val DEFAULT_LOCAL_NODE_TESTNET_API_URL: String = "http://127.0.0.1:9052/"

  /**
   * Get a time-zone timestamp.
   *
   * @param zone The desired timezone.
   * @return A time-zone timestamp, with date and time.
   */
  def getTimeStamp(zone: String): String = {

    // Get the date and time in UTC format
    val dateTime: LocalDateTime = LocalDateTime.now(ZoneId.of(zone))

    // Format the time string
    val date: String = dateTime.toString.split("[T]")(0)
    val time: String = dateTime.toString.split("[T]")(1).split("\\.")(0)
    val timestamp: String = s"[$zone $date $time]"
    timestamp

  }

  /**
   * Check if the api url is from the local node, either for MAINNET or TESTNET
   *
   * @param apiUrl Node API url.
   * @return The boolean value if the api url matched the local node url
   */
  def isLocalNodeApiUrl(apiUrl: String): Boolean = apiUrl.equals(DEFAULT_LOCAL_NODE_MAINNET_API_URL) || apiUrl.equals(DEFAULT_LOCAL_NODE_TESTNET_API_URL)

  def executeCardValueMappingSingletonTokenMinting(implicit setupConfig: TradeInSetupConfig, ctx: BlockchainContext, prover: ErgoProver): Unit = {

    println(Console.YELLOW + s"========== ${TradeInUtils.getTimeStamp("UTC")} EXECUTING TX: CARD-VALUE-MAPPING SINGLETON TOKEN MINTING ==========" + Console.RESET)

    // read the report
    val readReportConfigResult: Try[TradeInReportConfig] = TradeInReportConfig.load(TRADEIN_REPORT_CONFIG_FILE_PATH)
    val reportConfig: TradeInReportConfig = readReportConfigResult.get

    // Build the transaction
    val unsignedTx: UnsignedTransaction = CardValueMappingSingletonTokenMintingTxBuilder(setupConfig, reportConfig).build(ctx.newTxBuilder())
    val signedTx: SignedTransaction = prover.sign(unsignedTx)
    val txId: String = ctx.sendTransaction(signedTx).replaceAll("\"", "")

    // Print out tx status message
    println(Console.GREEN + s"========== ${TradeInUtils.getTimeStamp("UTC")} TX SUCCESSFUL: CARD-VALUE-MAPPING SINGLETON TOKEN MINTING ==========" + Console.RESET)
    println(Console.GREEN + s"========== ${TradeInUtils.getTimeStamp("UTC")} TX SAVED: CARD-VALUE-MAPPING SINGLETON TOKEN MINTING ==========" + Console.RESET)
    reportConfig.cardValueMappingIssuanceBox.boxId = signedTx.getOutputsToSpend.get(0).getId.toString
    reportConfig.cardValueMappingIssuanceBox.txId = txId
    TradeInReportConfig.write(TRADEIN_REPORT_CONFIG_FILE_PATH, reportConfig)

    // Print tx link to user
    println(Console.BLUE + s"========== ${TradeInUtils.getTimeStamp("UTC")} VIEW TX IN THE ERGO-EXPLORER WITH THE LINK BELOW ==========" + Console.RESET)
    if (ctx.getNetworkType.equals(NetworkType.MAINNET)) {
      println(TradeInUtils.ERGO_EXPLORER_TX_URL_PREFIX_MAINNET + txId)
    } else {
      println(TradeInUtils.ERGO_EXPLORER_TX_URL_PREFIX_TESTNET + txId)
    }

  }

  def executeGameLPBoxCreation(implicit setupConfig: TradeInSetupConfig, ctx: BlockchainContext, prover: ErgoProver): Unit = {

    println(Console.YELLOW + s"========== ${TradeInUtils.getTimeStamp("UTC")} EXECUTING TX: GAME LP BOX CREATION ==========" + Console.RESET)

    // read the report
    val readReportConfigResult: Try[TradeInReportConfig] = TradeInReportConfig.load(TRADEIN_REPORT_CONFIG_FILE_PATH)
    val reportConfig: TradeInReportConfig = readReportConfigResult.get

    // Build the transaction
    val unsignedTx: UnsignedTransaction = GameLPBoxCreationTxBuilder(setupConfig, reportConfig).build(ctx.newTxBuilder())
    val signedTx: SignedTransaction = prover.sign(unsignedTx)
    val txId: String = ctx.sendTransaction(signedTx).replaceAll("\"", "")

    // Print out tx status message
    println(Console.GREEN + s"========== ${TradeInUtils.getTimeStamp("UTC")} TX SUCCESSFUL: GAME LP BOX CREATION ==========" + Console.RESET)
    println(Console.GREEN + s"========== ${TradeInUtils.getTimeStamp("UTC")} TX SAVED: GAME LP BOX CREATION ==========" + Console.RESET)

    reportConfig.gameLPBox.txId = txId
    TradeInReportConfig.write(TRADEIN_REPORT_CONFIG_FILE_PATH, reportConfig)

    // Print tx link to user
    println(Console.BLUE + s"========== ${TradeInUtils.getTimeStamp("UTC")} VIEW TX IN THE ERGO-EXPLORER WITH THE LINK BELOW ==========" + Console.RESET)
    if (ctx.getNetworkType.equals(NetworkType.MAINNET)) {
      println(TradeInUtils.ERGO_EXPLORER_TX_URL_PREFIX_MAINNET + txId)
    } else {
      println(TradeInUtils.ERGO_EXPLORER_TX_URL_PREFIX_TESTNET + txId)
    }

  }

  def executeGameLPSingletonTokenMinting(implicit setupConfig: TradeInSetupConfig, ctx: BlockchainContext, prover: ErgoProver): Unit = {

    println(Console.YELLOW + s"========== ${TradeInUtils.getTimeStamp("UTC")} EXECUTING TX: GAME LP SINGLETON TOKEN MINTING ==========" + Console.RESET)

    // read the report
    val readReportConfigResult: Try[TradeInReportConfig] = TradeInReportConfig.load(TRADEIN_REPORT_CONFIG_FILE_PATH)
    val reportConfig: TradeInReportConfig = readReportConfigResult.get

    // Build the transaction
    val unsignedTx: UnsignedTransaction = GameLPSingletonTokenMintingTxBuilder(setupConfig, reportConfig).build(ctx.newTxBuilder())
    val signedTx: SignedTransaction = prover.sign(unsignedTx)
    val txId: String = ctx.sendTransaction(signedTx).replaceAll("\"", "")

    // Print out tx status message
    println(Console.GREEN + s"========== ${TradeInUtils.getTimeStamp("UTC")} TX SUCCESSFUL: GAME LP SINGLETON TOKEN MINTING ==========" + Console.RESET)
    println(Console.GREEN + s"========== ${TradeInUtils.getTimeStamp("UTC")} TX SAVED: GAME LP SINGLETON TOKEN MINTING ==========" + Console.RESET)
    reportConfig.gameLPIssuanceBox.boxId = signedTx.getOutputsToSpend.get(0).getId.toString
    reportConfig.gameLPIssuanceBox.txId = txId
    TradeInReportConfig.write(TRADEIN_REPORT_CONFIG_FILE_PATH, reportConfig)

    // Print tx link to user
    println(Console.BLUE + s"========== ${TradeInUtils.getTimeStamp("UTC")} VIEW TX IN THE ERGO-EXPLORER WITH THE LINK BELOW ==========" + Console.RESET)
    if (ctx.getNetworkType.equals(NetworkType.MAINNET)) {
      println(TradeInUtils.ERGO_EXPLORER_TX_URL_PREFIX_MAINNET + txId)
    } else {
      println(TradeInUtils.ERGO_EXPLORER_TX_URL_PREFIX_TESTNET + txId)
    }

  }

  def executeGameTokenMinting(implicit setupConfig: TradeInSetupConfig, ctx: BlockchainContext, prover: ErgoProver): Unit = {

    println(Console.YELLOW + s"========== ${TradeInUtils.getTimeStamp("UTC")} EXECUTING TX: GAME TOKEN MINTING ==========" + Console.RESET)

    // read the report
    val readReportConfigResult: Try[TradeInReportConfig] = TradeInReportConfig.load(TRADEIN_REPORT_CONFIG_FILE_PATH)
    val reportConfig: TradeInReportConfig = readReportConfigResult.get

    // Build the transaction
    val unsignedTx: UnsignedTransaction = GameTokenMintingTxBuilder(setupConfig, reportConfig).build(ctx.newTxBuilder())
    val signedTx: SignedTransaction = prover.sign(unsignedTx)
    val txId: String = ctx.sendTransaction(signedTx).replaceAll("\"", "")

    // Print out tx status message
    println(Console.GREEN + s"========== ${TradeInUtils.getTimeStamp("UTC")} TX SUCCESSFUL: GAME TOKEN MINTING ==========" + Console.RESET)
    println(Console.GREEN + s"========== ${TradeInUtils.getTimeStamp("UTC")} TX SAVED: GAME TOKEN MINTING ==========" + Console.RESET)
    reportConfig.gameTokenIssuanceBox.boxId = signedTx.getOutputsToSpend.get(0).getId.toString
    reportConfig.gameTokenIssuanceBox.txId = txId
    TradeInReportConfig.write(TRADEIN_REPORT_CONFIG_FILE_PATH, reportConfig)

    // Print tx link to user
    println(Console.BLUE + s"========== ${TradeInUtils.getTimeStamp("UTC")} VIEW TX IN THE ERGO-EXPLORER WITH THE LINK BELOW ==========" + Console.RESET)
    if (ctx.getNetworkType.equals(NetworkType.MAINNET)) {
      println(TradeInUtils.ERGO_EXPLORER_TX_URL_PREFIX_MAINNET + txId)
    } else {
      println(TradeInUtils.ERGO_EXPLORER_TX_URL_PREFIX_TESTNET + txId)
    }

  }

  def compileContracts(implicit setupConfig: TradeInSetupConfig, ctx: BlockchainContext): Unit = {

    println(Console.YELLOW + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILING CONTRACTS ==========" + Console.RESET)

    // compile player proxy contract
    val proxyResult = compilePlayerProxy
    if (proxyResult.isSuccess) {
      println(Console.GREEN + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILED SUCCESSFULLY: PLAYER PROXY CONTRACT ==========" + Console.RESET)
    } else {
      println(Console.RED + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILATION FAILED FOR: PLAYER PROXY CONTRACT ==========" + Console.RESET)
    }

    // compile card value mapping contract
    val cardValueMappingResult = compileCardValueMapping
    if (cardValueMappingResult.isSuccess) {
      println(Console.GREEN + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILED SUCCESSFULLY: CARD VALUE MAPPING CONTRACT ==========" + Console.RESET)
    } else {
      println(Console.RED + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILATION FAILED FOR: CARD VALUE MAPPING CONTRACT ==========" + Console.RESET)
    }

    // compile card value mapping issuance contract
    val cardValueMappingIssuanceResult = compileCardValueMappingIssuance
    if (cardValueMappingIssuanceResult.isSuccess) {
      println(Console.GREEN + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILED SUCCESSFULLY: CARD VALUE MAPPING ISSUANCE CONTRACT ==========" + Console.RESET)
    } else {
      println(Console.RED + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILATION FAILED FOR: CARD VALUE MAPPING ISSUANCE CONTRACT ==========" + Console.RESET)
    }

    // compile game lp contract
    val lpResult = compileGameLP
    if (lpResult.isSuccess) {
      println(Console.GREEN + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILED SUCCESSFULLY: GAME LP CONTRACT ==========" + Console.RESET)
    } else {
      println(Console.RED + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILATION FAILED FOR: GAME LP CONTRACT ==========" + Console.RESET)
      println(lpResult.get)
    }

    // compile game lp issuance contract
    val lpIssuanceResult = compileGameLPIssuance
    if (lpIssuanceResult.isSuccess) {
      println(Console.GREEN + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILED SUCCESSFULLY: GAME LP ISSUANCE CONTRACT ==========" + Console.RESET)
    } else {
      println(Console.RED + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILATION FAILED FOR: GAME LP ISSUANCE CONTRACT ==========" + Console.RESET)
      println(lpResult.get)
    }

    // compile game token issuance contract
    val gameTokenIssuanceResult = compileGameTokenIssuance
    if (gameTokenIssuanceResult.isSuccess) {
      println(Console.GREEN + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILED SUCCESSFULLY: GAME TOKEN ISSUANCE ==========" + Console.RESET)
    } else {
      println(Console.RED + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILATION FAILED FOR: GAME TOKEN ISSUANCE ==========" + Console.RESET)
      println(lpResult.get)
    }

    println(Console.GREEN + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILED CONTRACTS SUCCESSFULLY ==========" + Console.RESET)

  }

  def compileGameTokenIssuance(implicit setupConfig: TradeInSetupConfig, ctx: BlockchainContext): Try[Unit] = {

    println(Console.YELLOW + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILING: GAME TOKEN ISSUANCE ==========" + Console.RESET)

    // read the report
    val readReportConfigResult: Try[TradeInReportConfig] = TradeInReportConfig.load(TRADEIN_REPORT_CONFIG_FILE_PATH)
    val reportConfig = readReportConfigResult.get

    val contract: ErgoContract = GameTokenIssuanceContractBuilder(setupConfig, reportConfig).toErgoContract
    val contractString: String = Address.fromErgoTree(contract.getErgoTree, ctx.getNetworkType).toString

    // write to the report
    reportConfig.gameTokenIssuanceBox.gameTokenIssuanceContract = contractString
    TradeInReportConfig.write(TRADEIN_REPORT_CONFIG_FILE_PATH, reportConfig)

  }

  def compileGameLPIssuance(implicit setupConfig: TradeInSetupConfig, ctx: BlockchainContext): Try[Unit] = {

    println(Console.YELLOW + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILING: GAME LP ISSUANCE ==========" + Console.RESET)

    // read the report
    val readReportConfigResult: Try[TradeInReportConfig] = TradeInReportConfig.load(TRADEIN_REPORT_CONFIG_FILE_PATH)
    val reportConfig = readReportConfigResult.get

    val contract: ErgoContract = GameLPIssuanceContractBuilder(setupConfig, reportConfig).toErgoContract
    val contractString: String = Address.fromErgoTree(contract.getErgoTree, ctx.getNetworkType).toString

    // write to the report
    reportConfig.gameLPIssuanceBox.gameLPIssuanceContract = contractString
    TradeInReportConfig.write(TRADEIN_REPORT_CONFIG_FILE_PATH, reportConfig)

  }

  def compileGameLP(implicit setupConfig: TradeInSetupConfig, ctx: BlockchainContext): Try[Unit] = {

    println(Console.YELLOW + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILING: GAME LP ==========" + Console.RESET)

    // read the report
    val readReportConfigResult: Try[TradeInReportConfig] = TradeInReportConfig.load(TRADEIN_REPORT_CONFIG_FILE_PATH)
    val reportConfig = readReportConfigResult.get

    val contract: ErgoContract = GameLPContractBuilder(setupConfig, reportConfig).toErgoContract
    val contractString: String = Address.fromErgoTree(contract.getErgoTree, ctx.getNetworkType).toString

    // write to the report
    reportConfig.gameLPBox.gameLPContract = contractString
    TradeInReportConfig.write(TRADEIN_REPORT_CONFIG_FILE_PATH, reportConfig)

  }

  def compileCardValueMappingIssuance(implicit setupConfig: TradeInSetupConfig, ctx: BlockchainContext): Try[Unit] = {

    println(Console.YELLOW + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILING: CARD VALUE MAPPING ISSUANCE ==========" + Console.RESET)

    // read the report
    val readReportConfigResult: Try[TradeInReportConfig] = TradeInReportConfig.load(TRADEIN_REPORT_CONFIG_FILE_PATH)
    val reportConfig = readReportConfigResult.get

    val contract: ErgoContract = CardValueMappingIssuanceContractBuilder(setupConfig, reportConfig).toErgoContract
    val contractString: String = Address.fromErgoTree(contract.getErgoTree, ctx.getNetworkType).toString

    // write to the report
    reportConfig.cardValueMappingIssuanceBox.cardValueMappingIssuanceContract = contractString
    TradeInReportConfig.write(TRADEIN_REPORT_CONFIG_FILE_PATH, reportConfig)

  }

  def compileCardValueMapping(implicit setupConfig: TradeInSetupConfig, ctx: BlockchainContext): Try[Unit] = {

    println(Console.YELLOW + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILING: CARD VALUE MAPPING ==========" + Console.RESET)

    // read the report
    val readReportConfigResult: Try[TradeInReportConfig] = TradeInReportConfig.load(TRADEIN_REPORT_CONFIG_FILE_PATH)
    val reportConfig = readReportConfigResult.get

    val contract: ErgoContract = CardValueMappingContractBuilder().toErgoContract
    val contractString: String = Address.fromErgoTree(contract.getErgoTree, ctx.getNetworkType).toString

    // write to the report
    reportConfig.cardValueMappingBox.cardValueMappingContract = contractString
    TradeInReportConfig.write(TRADEIN_REPORT_CONFIG_FILE_PATH, reportConfig)

  }

  def compilePlayerProxy(implicit setupConfig: TradeInSetupConfig, ctx: BlockchainContext): Try[Unit] = {

    println(Console.YELLOW + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILING: PLAYER PROXY ==========" + Console.RESET)

    val contract: ErgoContract = PlayerProxyContractBuilder(setupConfig).toErgoContract
    val contractString: String = Address.fromErgoTree(contract.getErgoTree, ctx.getNetworkType).toString

    // read the report
    val readReportConfigResult: Try[TradeInReportConfig] = TradeInReportConfig.load(TRADEIN_REPORT_CONFIG_FILE_PATH)
    val reportConfig = readReportConfigResult.get

    // write to the report
    reportConfig.playerProxyBox.playerProxyContract = contractString
    TradeInReportConfig.write(TRADEIN_REPORT_CONFIG_FILE_PATH, reportConfig)

  }

}
