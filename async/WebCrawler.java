
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class WebCrawler {

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		boolean usingFuture = false;
		
		List<String> websites = Arrays.asList("http://en.wikipedia.org/", "https://www.facebook.com/");
		
		long time = System.currentTimeMillis();
		
		Set<String> result = usingFuture ? usingFuture(websites) :withoutUsingFuture(websites);
		
		System.out.println("total time: " + (System.currentTimeMillis() - time));
		
		System.out.println(result);
	}
	
	private static Set<String> withoutUsingFuture(List<String> websites) {	
		 return websites.stream()
						.map(WebCrawler :: getDocument)
						.map(WebCrawler :: processDocument)
						.flatMap(urls -> urls.stream().map(i -> i))
						.collect(Collectors.toSet());
	}
	
	private static Set<String> usingFuture(List<String> websites) throws InterruptedException, ExecutionException {
		List<CompletableFuture<Set<String>>> futureTasks = websites
				.stream()
				.map(site -> CompletableFuture.supplyAsync(() -> getDocument(site)))
				.map(document -> document
								  .thenApply(WebCrawler :: processDocument)
								  .exceptionally(th -> new HashSet()))
				.collect(Collectors.toList());
			
			
			//returns when all futures either completed or exception throws
			CompletableFuture<Void> completedFuture = CompletableFuture
														.allOf(futureTasks.toArray(
																new CompletableFuture[futureTasks.size()]));
			
			
			// we need to get list of urls instead void.
			CompletableFuture<Set<String>> result = completedFuture
				.thenApply(v -> futureTasks
										.stream()
										.map(future -> future.join())
										.flatMap(list -> list.stream().map(m -> m))
										.collect(Collectors.toSet())
						  );
			
			// here is the result
			return result.get();
	}

	private static Set<String> processDocument(Document document) {
		return document.select("a")
					.stream()
					.map(element -> element.attr("abs:href"))
					.collect(Collectors.toSet());
	}
	
	private static List<String> usingStream(String ...sites) {
		return Arrays.asList(sites)
                .stream()
                .map(WebCrawler :: getDocument)
                .map(doc -> doc.select("a"))
                .flatMap(elements -> elements.stream().map(element -> element.attr("abs:href")))
                .collect(Collectors.toList());
	}
	
	private static Document getDocument(String site)  {
		try {
			return Jsoup.connect(site).get();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
}
