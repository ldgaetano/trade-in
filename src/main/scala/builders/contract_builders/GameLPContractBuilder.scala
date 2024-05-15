package builders.contract_builders

import configs.report_config.TradeInReportConfig
import configs.setup_config.TradeInSetupConfig
import utils.TradeInUtils
import org.ergoplatform.appkit._
import sigmastate.Values
import special.collection.Coll
import special.sigma.SigmaProp
import sigmastate.Values.SigmaPropValue
import special.sigma.GroupElement
import utils.TradeInUtils.TRADEIN_REPORT_CONFIG_FILE_PATH

import scala.util.Try

case class GameLPContractBuilder(
                                  cardValueMappingContractBytes: ErgoValue[Coll[java.lang.Byte]],
                                  devPKGE: ErgoValue[GroupElement],
                                  devAddress: ErgoValue[Coll[java.lang.Byte]],
                                  tradeInFeeAddress: ErgoValue[Coll[java.lang.Byte]],
                                  setCreationMultiSigThreshold: Int,
                                  setCreationMultiSigAddressesGE: ErgoValue[Coll[GroupElement]]
                                ) extends TradeInContractBuilder {

    override val script: String = TradeInUtils.GAME_LP_SCRIPT

    override def toErgoContract(implicit ctx: BlockchainContext): ErgoContract = {

        ctx.compileContract(
            ConstantsBuilder.create()
              .item("$CardValueMappingContractBytes", cardValueMappingContractBytes.getValue)
              .item("$DevPKGE", devPKGE.getValue)
              .item("$DevAddress", devAddress.getValue)
              .item("$TradeInFeeAddress", tradeInFeeAddress.getValue)
              .item("$SetCreationMultiSigThreshold", setCreationMultiSigThreshold)
              .item("$SetCreationMultiSigAddressesGE", setCreationMultiSigAddressesGE.getValue)
              .build(),
            script
        )

    }

}

object GameLPContractBuilder {

    def apply(setupConfig: TradeInSetupConfig, reportConfig: TradeInReportConfig): GameLPContractBuilder = {

        val cardValueMappingContract: ErgoValue[Coll[java.lang.Byte]] = ErgoValue.of(Address.create(reportConfig.cardValueMappingBoxes.cardValueMappingContract).toPropositionBytes)

        val devPKGE: ErgoValue[GroupElement] = ErgoValue.of(Address.createEip3Address(
            setupConfig.node.wallet.index,
            setupConfig.node.networkType,
            SecretString.create(setupConfig.node.wallet.mnemonic),
            SecretString.create(setupConfig.node.wallet.password),
            false
        ).getPublicKeyGE)

        val devAddress: ErgoValue[Coll[java.lang.Byte]] = ErgoValue.of(Address.create(setupConfig.settings.devAddress).toPropositionBytes)
        val networkType: NetworkType = setupConfig.node.networkType
        val tradeInFeeAddressByNetworkType: String = if (networkType == NetworkType.MAINNET) TradeInUtils.TRADE_IN_FEE_MAINNET_ADDRESS else TradeInUtils.TRADE_IN_FEE_TESTNET_ADDRESS
        val tradeInFeeAddress: ErgoValue[Coll[java.lang.Byte]] = ErgoValue.of(Address.create(tradeInFeeAddressByNetworkType).toPropositionBytes)
        val multisigThreshold: Int = setupConfig.settings.setCreationMultiSig.threshold
        val multisigStrings: Array[String] = setupConfig.settings.setCreationMultiSig.addresses
        val multisigGEs: Array[GroupElement] = multisigStrings.map(s => ErgoValue.of(Address.create(s).getPublicKeyGE).getValue)
        val multisigAddresses: ErgoValue[Coll[GroupElement]] = ErgoValue.of(multisigGEs, ErgoType.groupElementType())

        new GameLPContractBuilder(
            cardValueMappingContract,
            devPKGE,
            devAddress,
            tradeInFeeAddress,
            multisigThreshold,
            multisigAddresses
        )

    }

    def compile(setupConfig: TradeInSetupConfig)(implicit ctx: BlockchainContext): Try[Unit] = {

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

}
