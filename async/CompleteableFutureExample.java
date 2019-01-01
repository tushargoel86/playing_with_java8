
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

public class CompleteableFutureExample {

	public static void main(String[] args) throws Exception {

		String home = "./";
		List<String> webSites = Arrays.asList("geeksOfGeeks.txt", "gmail.txt", "yahoo.txt", "monster.txt");

		ForkJoinPool pool = new ForkJoinPool(4);

		// we read the file data and pass to async function
		// once it completes it parse it.
		// as it returns future so we collect list of all competableFuture
		List<CompletableFuture<WebSite>> futureTasks = webSites.stream().map(site -> home + site)
				.map(site -> CompletableFuture.supplyAsync(() -> readFile(site), pool))
				.map(future -> future.thenApply(CompleteableFutureExample::parse)
						.exceptionally(CompleteableFutureExample::completeExceptionally))
				.collect(Collectors.toList());

		// returns when all futures are either completed or completeException
		// throws
		// only problem is, it returns void.
		CompletableFuture<Void> allFutures = CompletableFuture
				.allOf(futureTasks.toArray(new CompletableFuture[futureTasks.size()]));

		// we need to get list of website instead void.
		CompletableFuture<List<WebSite>> completedFutures = allFutures
				.thenApply(v -> futureTasks.stream().map(future -> future.join()).collect(Collectors.toList()));

		// now we get all the future we need to aplly finam computation
		CompletableFuture<Long> result = completedFutures.thenApply(
				relevance -> relevance.stream().filter(website -> "UTF-8".equalsIgnoreCase(website.getMeta())).count());

		// here is the result
		System.out.println(result.get());
		System.out.println(result.isCompletedExceptionally());

	}

	private static File readFile(String file) {
		System.out.println("reading file: " + file + " by thread " + Thread.currentThread());
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return new File(file);
	}

	//what to do in case failure
	private static WebSite completeExceptionally(Throwable th) {
		// throw new RuntimeException(th.getMessage());
		return new WebSite();
	}

	
	private static WebSite parse(File file) {
		// may want to fail some as well
		if (file.getName().equals("yahoo.txt")) {
			throw new IllegalArgumentException("yahoo is not allowed");
		}

		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(WebSite.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			return (WebSite) unmarshaller.unmarshal(file);
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}

}
