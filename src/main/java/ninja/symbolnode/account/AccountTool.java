package ninja.symbolnode.account;

import io.nem.symbol.sdk.model.account.*;
import io.nem.symbol.sdk.model.network.NetworkType;
import io.nem.symbol.sdk.model.transaction.*;
import io.nem.symbol.sdk.api.*;
import io.nem.symbol.sdk.infrastructure.vertx.*;
import java.util.concurrent.*;

public class AccountTool {
public static void main(String[] args) throws Exception {
  getAccount(args[0]);
}

  private static void getAccount(String pk) throws Exception {
    // Replace with a private key
        final String privateKey = pk;//"0000000000000000000000000000000000000000000000000000000000000000";
        final Account account = Account.createFromPrivateKey(privateKey, NetworkType.TEST_NET);

        System.out.printf("Your account address is: %s and its private key: %s",
            account.getAddress().plain(), account.getPrivateKey());
	accountPretty(account.getAddress().plain());
  }

  private static void accountPretty(String rawAddress) throws InterruptedException, ExecutionException {
          // replace with node endpoint
        try (final RepositoryFactory repositoryFactory = new RepositoryFactoryVertxImpl(
            "https://cola-potatochips.net:3001")) {
            final AccountRepository accountRepository = repositoryFactory
                .createAccountRepository();

            // Replace with an address
            //final String rawAddress = "TB6Q5E-YACWBP-CXKGIL-I6XWCH-DRFLTB-KUK34I-YJQ";
            final Address address = Address.createFromRawAddress(rawAddress);
            final AccountInfo accountInfo = accountRepository
                .getAccountInfo(address).toFuture().get();
            final JsonHelper helper = new JsonHelperJackson2();
            System.out.println(helper.prettyPrint(accountInfo));
        }
  }
}
