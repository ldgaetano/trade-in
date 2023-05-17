package app

import configs.setup_config.TradeInSetupConfig
import org.ergoplatform.appkit.{BlockchainContext, ErgoClient, NetworkType, RestApiErgoClient}
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

        if (txType.equals("--compile")) {

          // Compile contracts
          println(Console.YELLOW + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILING CONTRACTS ==========" + Console.RESET)
          TradeInUtils.compileContracts(setupConfig, ctx)
          println(Console.GREEN + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILED CONTRACTS SUCCESSFULLY ==========" + Console.RESET)

        }



//        // Execute transactions
//        if (txType.equals("--all")) {
//          // execute all transactions
//        } else if (txType.equals("--mint-game-tokens")) {
//
//          // execute mint game tokens transaction
//
//        }

      })

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
