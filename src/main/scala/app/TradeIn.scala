package app

import configs.setup_config.TradeInSetupConfig
import org.ergoplatform.appkit.{BlockchainContext, ErgoClient, ErgoProver, NetworkType, RestApiErgoClient, SecretString}
import utils.TradeInUtils

import scala.sys.exit
import scala.util.Try

object TradeIn {

  def main(args: Array[String]): Unit = {

    // Get the config settings
    val setupConfigLoadResult: Try[TradeInSetupConfig] = TradeInSetupConfig.load(TradeInUtils.TRADEIN_SETUP_CONFIG_FILE_PATH)

    if (setupConfigLoadResult.isSuccess) {

      // Print configuration load status
      println(Console.GREEN + s"========== ${TradeInUtils.getTimeStamp("UTC")} CONFIGURATIONS LOADED SUCCESSFULLY ==========" + Console.RESET)

      // Get configs
      val setupConfig: TradeInSetupConfig = setupConfigLoadResult.get

      // Setup Ergo Client
      val txType: String = args(0)
      val apiUrl: String = setupConfig.node.nodeApi.apiUrl
      val networkType: NetworkType = setupConfig.node.networkType
      val explorerURL: String = RestApiErgoClient.getDefaultExplorerUrl(networkType)
      val ergoClient: ErgoClient = RestApiErgoClient.create(apiUrl, networkType, "", explorerURL)
      println(Console.GREEN + s"========== ${TradeInUtils.getTimeStamp("UTC")} ERGO CLIENT CREATED SUCCESSFULLY ==========" + Console.RESET)

      // Print the TradeIn logo
      println(Console.MAGENTA + TradeInUtils.tradeInLogo + Console.RESET)

      ergoClient.execute((ctx: BlockchainContext) => {

        // Setup prover
        val prover: ErgoProver = ctx.newProverBuilder()
          .withMnemonic(
          SecretString.create(setupConfig.node.wallet.mnemonic),
          SecretString.create(setupConfig.node.wallet.password),
          false
          )
          .withEip3Secret(setupConfig.node.wallet.index)
          .build()

        if (txType.equals("--trade-in-setup")) {

          // Compile contracts
          TradeInUtils.compileContracts(setupConfig, ctx)

          // Execute all transactions
          TradeInUtils.executeGameTokenMinting(setupConfig, ctx, prover)
          TradeInUtils.executeGameLPSingletonTokenMinting(setupConfig, ctx, prover)

        } else if (txType.equals("--compile")) {

          // Compile contracts
          TradeInUtils.compileContracts(setupConfig, ctx)

        } else if (txType.equals("--execute")) {

          // Execute all transactions
          TradeInUtils.executeGameTokenMinting(setupConfig, ctx, prover)
          TradeInUtils.executeGameLPSingletonTokenMinting(setupConfig, ctx, prover)

        } else if (txType.equals("--mint-game-tokens")) {

          // Execute game token minting transaction
          TradeInUtils.executeGameTokenMinting(setupConfig, ctx, prover)

        } else if (txType.equals("--mint-game-lp-singleton-token")) {

          TradeInUtils.executeGameLPSingletonTokenMinting(setupConfig, ctx, prover)

        } else {

          throw new IllegalArgumentException("invalid command")

        }

      })

      exit(0)

    } else {

      // Print configuration load status
      println(Console.RED + s"========== ${TradeInUtils.getTimeStamp("UTC")} CONFIGURATIONS LOADED UNSUCCESSFULLY ==========" + Console.RESET)

      // Print Failure exception
      println(setupConfigLoadResult.get)

      // Return error exit code
      exit(1)

    }

  }

}
