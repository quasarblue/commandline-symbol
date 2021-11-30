package ninja.symbolnode.account;

import java.math.BigInteger;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

import io.nem.symbol.core.crypto.PublicKey;
import io.nem.symbol.sdk.api.AccountRepository;
import io.nem.symbol.sdk.api.RepositoryFactory;
import io.nem.symbol.sdk.api.RepositoryFactoryConfiguration;
import io.nem.symbol.sdk.api.TransactionPaginationStreamer;
import io.nem.symbol.sdk.api.TransactionRepository;
import io.nem.symbol.sdk.api.TransactionSearchCriteria;
import io.nem.symbol.sdk.infrastructure.vertx.JsonHelperJackson2;
import io.nem.symbol.sdk.infrastructure.vertx.RepositoryFactoryVertxImpl;
import io.nem.symbol.sdk.model.account.Account;
import io.nem.symbol.sdk.model.account.AccountInfo;
import io.nem.symbol.sdk.model.account.Address;
import io.nem.symbol.sdk.model.mosaic.Currency;
import io.nem.symbol.sdk.model.network.NetworkType;
import io.nem.symbol.sdk.model.transaction.JsonHelper;
import io.nem.symbol.sdk.model.transaction.Transaction;
import io.nem.symbol.sdk.model.transaction.TransactionGroup;
import io.nem.symbol.sdk.model.transaction.TransactionType;
import io.nem.symbol.sdk.model.transaction.TransferTransaction;
import io.reactivex.Observable;

public class AccountTool {

	private static Address address;
    private static Account account;
    
	public static void main(String[] args) throws Exception {
      getAccount(args[0]);
      getAddress(account.getAddress().plain());
  	  accountInfo();
  	  getMosaic();
    }

  private static void getAccount(String pk) throws Exception {
    // Replace with a private key
    final String privateKey = pk;//"0000000000000000000000000000000000000000000000000000000000000000";
    account = Account.createFromPrivateKey(privateKey, NetworkType.TEST_NET);
    System.out.printf("Your account address is: %s and its private key: %s", account.getAddress().plain(), account.getPrivateKey());
    
  }

  private static void getAddress(String rawAddress) throws InterruptedException, ExecutionException {
    // replace with node endpoint
      
    // Replace with an address
    //final String rawAddress = "TB6Q5E-YACWBP-CXKGIL-I6XWCH-DRFLTB-KUK34I-YJQ";
    address = Address.createFromRawAddress(rawAddress);  
    
  }
  
  private static void accountInfo() throws InterruptedException, ExecutionException {
	  try (
		RepositoryFactory repositoryFactory = new RepositoryFactoryVertxImpl("https://cola-potatochips.net:3001")) {
		AccountRepository accountRepository = repositoryFactory.createAccountRepository();
	    final AccountInfo accountInfo = accountRepository.getAccountInfo(address).toFuture().get();
        final JsonHelper helper = new JsonHelperJackson2();
        System.out.println(helper.prettyPrint(accountInfo));  
	  }
  }
  
  private static void getMosaic() throws ExecutionException, InterruptedException {
	  // replace with signer public key
      PublicKey signerPublicKey = account.getPublicAccount().getPublicKey(); //fromHexString("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
      // replace with recipient address
      String recipientRawAddress = account.getAddress().plain();
    		  //"TCKV2SZ6NTQ36LEJAQZ7YJT5I6HH6PXXBUNT6GI";
    		  //// "TB6Q5E-YACWBP-CXKGIL-I6XWCH-DRFLTB-KUK34I-YJQ";
      Address recipientAddress = Address.createFromRawAddress(recipientRawAddress);
      //RepositoryFactory repositoryFactory = new RepositoryFactoryVertxImpl(recipientRawAddress);
      try (
    	RepositoryFactory repositoryFactory = new RepositoryFactoryVertxImpl("https://cola-potatochips.net:3001")) {    	        
        Currency networkCurrency = repositoryFactory.getNetworkCurrency().toFuture().get();
      
        final TransactionRepository transactionRepository = repositoryFactory.createTransactionRepository();

        TransactionPaginationStreamer streamer = new TransactionPaginationStreamer(transactionRepository);

        Observable<Transaction> transactions = streamer.search(new TransactionSearchCriteria(TransactionGroup.CONFIRMED).transactionTypes(
              Collections.singletonList(TransactionType.TRANSFER))
              .recipientAddress(recipientAddress)
              .signerPublicKey(signerPublicKey));

        BigInteger total = transactions.map(t -> (TransferTransaction) t)
          .flatMap(t -> Observable.fromIterable(t.getMosaics())).filter(mosaic ->
              networkCurrency.getMosaicId().map(mosaicId -> mosaic.getId().equals(mosaicId))
                  .orElse(false) || networkCurrency.getNamespaceId()
                  .map(mosaicId -> mosaic.getId().equals(mosaicId)).orElse(false)).reduce(
              BigInteger.ZERO, (acc, mosaic) -> acc.add(mosaic.getAmount())).toFuture()
          .get();

        System.out.println("Total " + networkCurrency.getUnresolvedMosaicId().getIdAsHex() +
          " sent to account " + recipientAddress.pretty() +
          " is:" + total);
      }
      //repositoryFactory.close();
  }
  
}
