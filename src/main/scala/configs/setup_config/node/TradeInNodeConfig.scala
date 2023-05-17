package configs.setup_config.node

import org.ergoplatform.appkit.NetworkType

case class TradeInNodeConfig(
                           nodeApi: TradeInNodeApiConfig,
                           wallet: TradeInWalletConfig,
                           networkType: NetworkType
                           )
