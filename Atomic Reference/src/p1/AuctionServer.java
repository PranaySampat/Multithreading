package p1;



/**
 *  @author PranaySampat
 */


import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;





public class AuctionServer
{
	/**
	 * Singleton: the following code makes the server a Singleton. You should
	 * not edit the code in the following noted section.
	 * 
	 * For test purposes, we made the constructor protected. 
	 */

	/* Singleton: Begin code that you SHOULD NOT CHANGE! */
	protected AuctionServer()
	{
	}

	private static AuctionServer instance = new AuctionServer();

	public static AuctionServer getInstance()
	{
		return instance;
	}

	/* Singleton: End code that you SHOULD NOT CHANGE! */





	/* Statistic variables and server constants: Begin code you should likely leave alone. */


	/**
	 * Server statistic variables and access methods:
	 */
	//private int soldItemsCount = 0;
	private AtomicInteger soldItemsCount = new AtomicInteger(0);
	//private int revenue = 0;
	private AtomicInteger revenue = new AtomicInteger(0);

	public int soldItemsCount()
	{
		return soldItemsCount.get();
	}

	public int revenue()
	{
		return revenue.get();
	}



	/**
	 * Server restriction constants:
	 */
	public static final int maxBidCount = 10; // The maximum number of bids at any given time for a buyer.
	public static final int maxSellerItems = 20; // The maximum number of items that a seller can submit at any given time.
	public static final int serverCapacity = 80; // The maximum number of active items at a given time.
	
	/* Statistic variables and server constants: End code you should likely leave alone. */



	/**
	 * Some variables we think will be of potential use as you implement the server...
	 */

	// List of items currently up for bidding (will eventually remove things that have expired).
	private List<Item> itemsUpForBidding = new ArrayList<Item>();
	private AtomicReference<List<Item>> aitemsUpForBidding = new AtomicReference<List<Item>>(new ArrayList<>(itemsUpForBidding));

	// The last value used as a listing ID.  We'll assume the first thing added gets a listing ID of 0.
	//private int lastListingID = -1; 
	private AtomicInteger  lastListingID = new AtomicInteger(-1);

	// List of item IDs and actual items.  This is a running list with everything ever added to the auction.
	private HashMap<Integer, Item> itemsAndIDs = new HashMap<Integer, Item>();
	private AtomicReference<HashMap<Integer, Item>> aitemsAndIDs = new AtomicReference<HashMap<Integer,Item>>(new HashMap<>(itemsAndIDs));

	// List of itemIDs and the highest bid for each item.  This is a running list with everything ever added to the auction.
	private HashMap<Integer, Integer> highestBids = new HashMap<Integer, Integer>();
	private AtomicReference<HashMap<Integer, Integer>> aHighestBids = new AtomicReference<HashMap<Integer,Integer>>(new HashMap<>(highestBids));
	// List of itemIDs and the person who made the highest bid for each item.   This is a running list with everything ever bid upon.
	private HashMap<Integer, String> highestBidders = new HashMap<Integer, String>(); 
	private AtomicReference<HashMap<Integer, String>> ahighestBidders = new AtomicReference<HashMap<Integer,String>>(new HashMap<>(highestBidders));
	private Map<String, Integer> itemsOverpriced = new HashMap<>();
	private AtomicReference<Map<String , Integer>> aitemsOverpriced = new AtomicReference<Map<String,Integer>>(new HashMap<>(itemsOverpriced));
	private Map<String, Boolean> disqualificationMap = new HashMap<>();
	private AtomicReference<Map<String, Boolean>> adisqualificationMap = new AtomicReference<>(new HashMap<>(disqualificationMap));
	private Map<String,Integer> biddingViolations = new HashMap<>();
	private AtomicReference<Map<String, Integer>> aBiddingViolations = new AtomicReference<Map<String,Integer>>(new HashMap<>(biddingViolations));
	private Map<String, Boolean> bidderDisqualification = new HashMap<>();
	private AtomicReference<Map<String, Boolean>> aBidderDisqualification = new AtomicReference<Map<String,Boolean>>(new HashMap<>(bidderDisqualification));
	private Map<String, Integer> durationMap = new HashMap<>();
	private AtomicReference<Map<String,Integer>> adurationMap = new AtomicReference<Map<String,Integer>>(new HashMap<>(durationMap));
	

	// List of sellers and how many items they have currently up for bidding.
	private HashMap<String, Integer> itemsPerSeller = new HashMap<String, Integer>();
	private AtomicReference<HashMap<String, Integer>> aitemsPerSeller = new AtomicReference<HashMap<String,Integer>>(new HashMap<>(itemsPerSeller));

	// List of buyers and how many items on which they are currently bidding.
	private HashMap<String, Integer> itemsPerBuyer = new HashMap<String, Integer>();
	private AtomicReference<HashMap<String, Integer>> aitemsPerBuyer = new AtomicReference<HashMap<String,Integer>>(new HashMap<>(itemsPerBuyer));



	// Object used for instance synchronization if you need to do it at some point 
	// since as a good practice we don't use synchronized (this) if we are doing internal
	// synchronization.
	//
	// private Object instanceLock = new Object(); 
//	private static final Object itemsUpForBiddingLock = new Object();
//	private static final Object itemsAndIDsLock = new Object();
//	//private static final Object highestBidsLock = new Object();
//	private static final Object highestBiddersLock = new Object();
//	private static final Object itemPerBuyerSellerLock = new Object();
//	//private static final Object itemsPerBuyerLock = new Object();
	

	private int counterforSeller = 0;
	private AtomicInteger aCountForSeller = new AtomicInteger(0);
	private int counterforBuyer = 0;
	private AtomicInteger aCountForBuyer = new AtomicInteger(0);
	private volatile  HashMap<Integer, Item> currentItemsAndIds ;
	
	
	



	/*
	 *  The code from this point forward can and should be changed to correctly and safely 
	 *  implement the methods as needed to create a working multi-threaded server for the 
	 *  system.  If you need to add Object instances here to use for locking, place a comment
	 *  with them saying what they represent.  Note that if they just represent one structure
	 *  then you should probably be using that structure's intrinsic lock.
	 */


	/**
	 * Attempt to submit an <code>Item</code> to the auction
	 * @param sellerName Name of the <code>Seller</code>
	 * @param itemName Name of the <code>Item</code>
	 * @param lowestBiddingPrice Opening price
	 * @param biddingDurationMs Bidding duration in milliseconds
	 * @return A positive, unique listing ID if the <code>Item</code> listed successfully, otherwise -1
	 */
	@SuppressWarnings({ "null", "unused" })
	public int submitItem(String seller, String itemName, int lowestBiddingPrice, int biddingDurationMs)
	{
		// TODO: IMPLEMENT CODE HERE
		// Some reminders:
		//   Make sure there's room in the auction site.
		//   If the seller is a new one, add them to the list of sellers.
		//   If the seller has too many items up for bidding, don't let them add this one.
		//   Don't forget to increment the number of things the seller has currently listed.
			
			//if(seller != null && seller.isEmpty() && itemName != null && itemName.isEmpty() && lowestBiddingPrice > 0 && biddingDurationMs > 0){
				//checking room
	
		if(seller.isEmpty() || seller.equals(null) || itemName.isEmpty() || itemName.equals(null) || lowestBiddingPrice <=0 || biddingDurationMs < 0){
			return -1;
		}
		int listingId=0;
		Item toItem = null;
		//checkSeller is disqualified
		Map<String,Boolean> disqualificationcheck = adisqualificationMap.get();
		if(disqualificationcheck.containsKey(seller) && disqualificationcheck.get(seller) == true){
			return -1;
		}
		//making a deep copy of arraylist to avoid concurrent modification
		List<Item> currentItemList = new ArrayList<>();
		currentItemList.addAll(aitemsUpForBidding.get());
		//check server capacity
		if(currentItemList != null && !currentItemList.isEmpty()){
			if(currentItemList.size() >= serverCapacity){
				return -1;
			}
		}
		//check whether seller has capacity to submit an item
	
		//now lets check whether item has been already submitted
		
		//HashSet<Integer> keyset = new HashSet<>(currentItemsAndIds.keySet());
		
//		currentItemsAndIds = aitemsAndIDs.get();
//		for(int id : currentItemsAndIds.keySet()){
//			if(currentItemsAndIds.get(id) != null && currentItemsAndIds.get(id).name() != null){
//				if(currentItemsAndIds.get(id).name().equals(itemName)){
//					return -1;
//				}
//			}
//		}
		//check whether seller has capacity to submit an item, if yes update the count
		HashMap<String, Integer> oldSellerItems = aitemsPerSeller.get();
		HashMap<String, Integer> currentSellerItems = aitemsPerSeller.get();
		if(currentSellerItems.containsKey(seller)){
			if(currentSellerItems.get(seller) > maxSellerItems){
				return -1;
			}else{
				currentSellerItems.put(seller,currentSellerItems.get(seller) + 1);
				aitemsPerSeller.compareAndSet(oldSellerItems, currentSellerItems);
			}
		}else{
			currentSellerItems.put(seller,1);
			aitemsPerSeller.compareAndSet(oldSellerItems, currentSellerItems);
		}
		Map<String,Integer> oldOverPricedItem = aitemsOverpriced.get();
		Map<String,Integer> currentOverPricedItem = aitemsOverpriced.get();
		if(lowestBiddingPrice > 99){
			if(currentOverPricedItem.containsKey(seller)){
				currentOverPricedItem.put(seller,currentOverPricedItem.get(seller) + 1);
				aitemsOverpriced.compareAndSet(oldOverPricedItem, currentOverPricedItem);
			}else{
				currentOverPricedItem.put(seller,1);
				aitemsOverpriced.compareAndSet(oldOverPricedItem, currentOverPricedItem);
			}
		}
		Map<String,Boolean> oldDisqualificationMap = adisqualificationMap.get();
		Map<String,Boolean> currentDisMap = adisqualificationMap.get();
		Map<String,Integer> updatedOverPricedList = aitemsOverpriced.get();
		if(updatedOverPricedList.containsKey(seller)){
			if(updatedOverPricedList.get(seller) >=3){
				currentDisMap.put(seller, true);
				aBidderDisqualification.compareAndSet(oldDisqualificationMap, currentDisMap);
			}
		}
		Map<String,Integer> oldBiddingDuration = adurationMap.get();
		Map<String,Integer> newBiddingDuration = adurationMap.get();
		if(biddingDurationMs == 0){
			if(newBiddingDuration.containsKey(seller)){
				newBiddingDuration.put(seller, newBiddingDuration.get(seller) + 1);
				adurationMap.compareAndSet(oldBiddingDuration, newBiddingDuration);
			}else{
				newBiddingDuration.put(seller, 1);
				adurationMap.compareAndSet(oldBiddingDuration, newBiddingDuration);
			}
		}
		Map<String,Integer> updatedBiddingDuration = adurationMap.get();
		Map<String,Boolean> updateDisqualificationMap = adisqualificationMap.get();
		Map<String,Boolean> updateDisqualificationMap1 = adisqualificationMap.get();
		
		if(updatedBiddingDuration.containsKey(seller)){
			if(updatedBiddingDuration.get(seller) >=5){
				updateDisqualificationMap1.put(seller, true);
				adisqualificationMap.compareAndSet(updateDisqualificationMap, updateDisqualificationMap1);
			}
		}
		HashMap<Integer, Item> oldItemAndIds = aitemsAndIDs.get();
		HashMap<Integer, Item> newItemAndIds = aitemsAndIDs.get();
		listingId = lastListingID.incrementAndGet();
		toItem = new Item(seller,itemName, listingId, lowestBiddingPrice, biddingDurationMs);
		newItemAndIds.put(listingId, toItem);
		aitemsAndIDs.compareAndSet(oldItemAndIds, newItemAndIds);
		List<Item> oldList = aitemsUpForBidding.get();
		//oldList.addAll(aitemsUpForBidding.get());
		List<Item> newList = aitemsUpForBidding.get();
		//newList.addAll(aitemsUpForBidding.get());
		newList.add(toItem);
		aitemsUpForBidding.compareAndSet(currentItemList, newList);
		return listingId;
		
		}
		
		

	
	
	
					
		
	//	return -1;
			
		
	
	
			
	



	/**
	 * Get all <code>Items</code> active in the auction
	 * @return A copy of the <code>List</code> of <code>Items</code>
	 */
	public List<Item> getItems()
	{
		// TODO: IMPLEMENT CODE HERE
		// Some reminders:
		//    Don't forget that whatever you return is now outside of your control.
			//synchronized (itemsUpForBiddingLock) {
		 List<Item> itemsUpForBidding = aitemsUpForBidding.get();
         if (itemsUpForBidding != null) {
             ArrayList<Item> toReturn;
             //int index = rand.nextInt(itemsUpForBidding.size());
             //Item item = itemsUpForBidding.get(index);

             toReturn = new ArrayList<Item>(itemsUpForBidding);
             //toReturn.add(item);
             return toReturn;
         } else {
             itemsUpForBidding = new ArrayList<Item>();
             ArrayList<Item> toReturn;
             toReturn = new ArrayList<Item>(itemsUpForBidding);
             return toReturn;
         }
     }

			
			
		
		
	


	/**
	 * Attempt to submit a bid for an <code>Item</code>
	 * @param bidderName Name of the <code>Bidder</code>
	 * @param listingID Unique ID of the <code>Item</code>
	 * @param biddingAmount Total amount to bid
	 * @return True if successfully bid, false otherwise
	 */
	public boolean submitBid(String bidder, int listingID, int biddingAmount)
	{
		// TODO: IMPLEMENT CODE HERE
		// Some reminders:
		//   See if the item exists.
		//   See if it can be bid upon.
		//   See if this bidder has too many items in their bidding list.
		//   Get current bidding info.
		//   See if they already hold the highest bid.
		//   See if the new bid isn't better than the existing/opening bid floor.
		//   Decrement the former winning bidder's count
		//   Put your bid in place
	if(bidder.equals(null) && bidder.isEmpty() && listingID < 0 && biddingAmount < 0){
		return false;
	}
	//synchronized (itemsUpForBiddingLock) {
			//First check whether bidder is disqualified
			Map<String,Boolean> checkBidderStatus = aBidderDisqualification.get();
			if(checkBidderStatus.containsKey(bidder)){
				return false;
			}
			//now lets check whether item is present or not
			Item item = null;
			HashMap<Integer, Item> checkItemsAndIds = aitemsAndIDs.get();
			item = checkItemsAndIds.get(listingID);
			if(item == null){
				return false;
			}
			//making deep copy
			List<Item> checkingList = new ArrayList<>();
			checkingList.addAll(aitemsUpForBidding.get());
			if(!checkingList.contains(item)){
				return false;
			}
			//checking whether bidding is open or not
			if(checkingList.contains(item) && item.biddingOpen() == false){
				return false;
			}
			//checking whether bidder has too many bids
			HashMap<String, Integer> oldItemsPerBidder = aitemsPerBuyer.get();
			HashMap<String, Integer> currentItemsPerBidder = aitemsPerBuyer.get();
			if(currentItemsPerBidder.containsKey(bidder)){
				if(currentItemsPerBidder.get(bidder) > maxBidCount){
					return false;
				}else{
					currentItemsPerBidder.put(bidder, currentItemsPerBidder.get(bidder) + 1);
					aitemsPerBuyer.compareAndSet(oldItemsPerBidder, currentItemsPerBidder);
				}
			}else{
				currentItemsPerBidder.put(bidder,1);
				aitemsPerBuyer.compareAndSet(oldItemsPerBidder, currentItemsPerBidder);
			}
			// checking if bid submitted is less than opening price
			if(item.lowestBiddingPrice() > biddingAmount){
				Map<String,Integer> oldBiddingViolation = aBiddingViolations.get();
				Map<String,Integer> currentBiddingViolation = aBiddingViolations.get();
				if(currentBiddingViolation.containsKey(bidder)){
					currentBiddingViolation.put(bidder, currentBiddingViolation.get(bidder) + 1);
					aBiddingViolations.compareAndSet(oldBiddingViolation, currentBiddingViolation);
				}else{
					currentBiddingViolation.put(bidder,1);
					aBiddingViolations.compareAndSet(oldBiddingViolation, currentBiddingViolation);
				}
			}
			//setting bidder disqualification based on bidding violations
			Map<String,Integer> biddingViolations = aBiddingViolations.get();
			Map<String,Boolean> oldDisqualCheck = adisqualificationMap.get();
			Map<String,Boolean> newDisqualCheck = adisqualificationMap.get();
			if(biddingViolations.containsKey(bidder) && biddingViolations.get(bidder) > 3){
				newDisqualCheck.put(bidder,true);
				adisqualificationMap.compareAndSet(oldDisqualCheck, newDisqualCheck);
			}
			//now lets check whether the bidder is eligible to bid comparing with highestBidders
			// and whether others have bidded on item or not
			HashMap<Integer, String> oldHighestBidders = ahighestBidders.get();
			HashMap<Integer, String> highestBidderCheck = ahighestBidders.get();
			HashMap<Integer, Integer> oldHighestBids = aHighestBids.get();
			HashMap<Integer, Integer> bidsCheck = aHighestBids.get();
			if(highestBidderCheck.containsKey(listingID)){
				if(highestBidderCheck.get(listingID).equalsIgnoreCase(bidder)){
					return false;
				}else if(bidsCheck.containsKey(listingID)){
					//lets check if highestBid is less than currentBid
					if( bidsCheck.get(listingID) > biddingAmount){
						return false;
					}else{
						bidsCheck.put(listingID, biddingAmount);
						aHighestBids.compareAndSet(oldHighestBids, bidsCheck);
						highestBidderCheck.put(listingID, bidder);
						ahighestBidders.compareAndSet(oldHighestBidders, highestBidderCheck);
						return true;
					}
				}	
			}else{
				if(item.lowestBiddingPrice() < biddingAmount){
				bidsCheck.put(listingID, biddingAmount);
				aHighestBids.compareAndSet(oldHighestBids, bidsCheck);
				highestBidderCheck.put(listingID, bidder);
				ahighestBidders.compareAndSet(oldHighestBidders, highestBidderCheck);
				return true;
				}
			}
			

			
			return false;
	}	

	/**
	 * Check the status of a <code>Bidder</code>'s bid on an <code>Item</code>
	 * @param bidderName Name of <code>Bidder</code>
	 * @param listingID Unique ID of the <code>Item</code>
	 * @return 1 (success) if bid is over and this <code>Bidder</code> has won<br>
	 * 2 (open) if this <code>Item</code> is still up for auction<br>
	 * 3 (failed) If this <code>Bidder</code> did not win or the <code>Item</code> does not exist
	 */
	public int checkBidStatus(String bidderName, int listingID)
	{
		// TODO: IMPLEMENT CODE HERE
		// Some reminders:
		//   If the bidding is closed, clean up for that item.
		//     Remove item from the list of things up for bidding.
		//     Decrease the count of items being bid on by the winning bidder if there was any...
		//     Update the number of open bids for this seller
		if(bidderName.equals(null) || listingID < 0){
			return 3;
		}
		//lets first check if bidding is still open
		Item item = null;
		HashMap<Integer,Item> itemandIdscheck = aitemsAndIDs.get();
		item = itemandIdscheck.get(listingID);
		//retreive currentList
		List<Item> currentList = new ArrayList<>();
		currentList.addAll(aitemsUpForBidding.get());
		if(item != null && currentList.contains(item) && item.biddingOpen() == true){
			return 2;
		}
		if(item != null && currentList.contains(item) && item.biddingOpen() == false){
			//lets remove the item from bidder & seller
			HashMap<String, Integer> itemsPerSellerCount = aitemsPerSeller.get();
			HashMap<String, Integer> itemsPerSellerCount2 = aitemsPerSeller.get();
			itemsPerSellerCount2.put(item.seller(), itemsPerSellerCount2.get(item.seller())-1);
			aitemsPerSeller.compareAndSet(itemsPerSellerCount, itemsPerSellerCount2);
			HashMap<String, Integer> itemPerBuyersCount = aitemsPerBuyer.get();
			HashMap<String, Integer> itemPerBuyersCount2 = aitemsPerBuyer.get();
			itemPerBuyersCount2.put(bidderName, itemPerBuyersCount2.get(bidderName)-1);
			aitemsPerBuyer.compareAndSet(itemPerBuyersCount, itemPerBuyersCount2);
			//Remove from running list
			List<Item> newList1 = new ArrayList<>();
			List<Item> newList12 = new ArrayList<>();
			newList1.addAll(aitemsUpForBidding.get());
			newList12.addAll(aitemsUpForBidding.get());
			HashMap<Integer, Integer> currentHighestBids = aHighestBids.get();
			HashMap<Integer, String> currentHighestBidders = ahighestBidders.get();
			if(currentHighestBids.containsKey(listingID) && currentHighestBidders.get(listingID).equals(bidderName)){
				soldItemsCount.set(soldItemsCount.get() + 1);
				//revenue = revenue.get() + currentHighestBids.get(listingID);
				revenue.set(revenue.get() + currentHighestBids.get(listingID));
				newList12.remove(item);
				aitemsUpForBidding.compareAndSet(newList1, newList12);
				return 1;
			}else{
				return 3;
			}
			
		}
		return 3;
		
	}

	/**
	 * Check the current bid for an <code>Item</code>
	 * @param listingID Unique ID of the <code>Item</code>
	 * @return The highest bid so far or the opening price if no bid has been made,
	 * -1 if no <code>Item</code> exists
	 */
	public int itemPrice(int listingID)
	{
		// TODO: IMPLEMENT CODE HERE
		
		if(listingID >= 0){
			Item item = null;
			//synchronized (lock1) {
			HashMap<Integer, Item> itemAndIDsCheck = aitemsAndIDs.get();
				item = itemAndIDsCheck.get(listingID);
			
			if (null != item) {
				//synchronized (lock2) {
				HashMap<Integer, Integer> highestBidsCheck = aHighestBids.get();
					if (highestBidsCheck.containsKey(listingID)) {
						return highestBidsCheck.get(listingID);
					} else {
						return item.lowestBiddingPrice();
					}
				
			}
		}
	return -1;
	}

	/**
	 * Check whether an <code>Item</code> has been bid upon yet
	 * @param listingID Unique ID of the <code>Item</code>
	 * @return True if there is no bid or the <code>Item</code> does not exist, false otherwise
	 */
	public Boolean itemUnbid(int listingID)
	{
		// TODO: IMPLEMENT CODE HERe
		Item item = null;
		if(listingID >=0){
			HashMap<Integer, Item> itemAndIdsCheck = new HashMap<>();
			itemAndIdsCheck.putAll(aitemsAndIDs.get());
			item = itemAndIdsCheck.get(listingID);
			List<Item> currentActiveList = new ArrayList<>();
			currentActiveList.addAll(aitemsUpForBidding.get());
			if(item != null && !currentActiveList.contains(item) ){
				return true;
			}
			HashMap<Integer, Integer> highestBidsCheck = new HashMap<>();
			highestBidsCheck.putAll(aHighestBids.get());
			if(highestBidsCheck.containsKey(listingID)){
				return true;
			}
		}
		return false;
	}
	public int returnSumofHighestBids(){
		//int sum =0;
		AtomicInteger sum = new AtomicInteger(0);
		HashMap<Integer, Integer> highestBidsCount = new HashMap<>();
		highestBidsCount.putAll(aHighestBids.get());
		for(int id : highestBidsCount.keySet()){
			//sum = sum + highestBidsCount.get(id);
			sum.set(sum.get() + highestBidsCount.get(id));
		}
		return sum.get();
	}
public int returnItemSize(){
	return aitemsAndIDs.get().size();
}
public int returnLastingId(){
	return lastListingID.get();
}
public int returnHighestBidders(){
	return ahighestBidders.get().size();
}

}
 