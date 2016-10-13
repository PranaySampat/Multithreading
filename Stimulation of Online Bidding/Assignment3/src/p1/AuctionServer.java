package p1;

/**
 *  @author PranaySampat
 */


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



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
	private int soldItemsCount = 0;
	private int revenue = 0;

	public int soldItemsCount()
	{
		return this.soldItemsCount;
	}

	public int revenue()
	{
		return this.revenue;
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


	// The last value used as a listing ID.  We'll assume the first thing added gets a listing ID of 0.
	private int lastListingID = -1; 

	// List of item IDs and actual items.  This is a running list with everything ever added to the auction.
	private HashMap<Integer, Item> itemsAndIDs = new HashMap<Integer, Item>();

	// List of itemIDs and the highest bid for each item.  This is a running list with everything ever added to the auction.
	private HashMap<Integer, Integer> highestBids = new HashMap<Integer, Integer>();

	// List of itemIDs and the person who made the highest bid for each item.   This is a running list with everything ever bid upon.
	private HashMap<Integer, String> highestBidders = new HashMap<Integer, String>(); 
	
	private Map<String, Integer> itemsOverpriced = new HashMap<>();
	private Map<String, Boolean> disqualificationMap = new HashMap<>();
	private Map<String,Integer> biddingViolations = new HashMap<>();
	private Map<String, Boolean> bidderDisqualification = new HashMap<>();
	private Map<String, Integer> durationMap = new HashMap<>();

	

	// List of sellers and how many items they have currently up for bidding.
	private HashMap<String, Integer> itemsPerSeller = new HashMap<String, Integer>();

	// List of buyers and how many items on which they are currently bidding.
	private HashMap<String, Integer> itemsPerBuyer = new HashMap<String, Integer>();



	// Object used for instance synchronization if you need to do it at some point 
	// since as a good practice we don't use synchronized (this) if we are doing internal
	// synchronization.
	//
	// private Object instanceLock = new Object(); 
	private static final Object itemsUpForBiddingLock = new Object();
	private static final Object itemsAndIDsLock = new Object();
	//private static final Object highestBidsLock = new Object();
	private static final Object highestBiddersLock = new Object();
	private static final Object itemPerBuyerSellerLock = new Object();
	//private static final Object itemsPerBuyerLock = new Object();
	

	private int counterforSeller = 0;
	private int counterforBuyer = 0;
	
	
	



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
		//if seller is disqualified
		
		else{
			synchronized (itemsUpForBiddingLock) {
				if(durationMap.containsKey(seller) && durationMap.get(seller) >= 5){
					return -1;}
				if(disqualificationMap.containsKey(seller) == true){
					return -1;}
				int listingId = 0;
				if(itemsUpForBidding.size() >= serverCapacity){
					return -1;
				}else{
					if(itemsUpForBidding != null){
						for(Item item : itemsUpForBidding){
							if(item.seller().equals(seller) && item.lowestBiddingPrice() > 99){
								itemsOverpriced.put(seller, itemsOverpriced.get(seller) + 1);
							}
						}
						if(itemsOverpriced.containsKey(seller)){
							if(itemsOverpriced.get(seller) > 3){
							disqualificationMap.put(seller, true);
							return -1;}
						}else{
							if(lowestBiddingPrice > 99)
							itemsOverpriced.put(seller,0);
						}
					}else{
						if(lowestBiddingPrice > 99){
							if(itemsOverpriced.containsKey(seller)){
								itemsOverpriced.put(seller,itemsOverpriced.get(seller) + 1);
							}else{
								itemsOverpriced.put(seller,0);
							}
						}
						if(biddingDurationMs == 0){
							if(durationMap.containsKey(seller)){
							durationMap.replace(seller, durationMap.get(seller) + 1);	
							}else{
								durationMap.put(seller, 1);
							}
						}
					}
					Item toAddItem = null;
					synchronized (itemsAndIDsLock) {
						for(int id : itemsAndIDs.keySet()){
							if(itemsAndIDs.get(id) != null && itemsAndIDs.get(id).name() != null){
								if(itemsAndIDs.get(id).name().equals(itemName)){
									return -1;
								}
							}
						}
						
						
						listingId = itemsAndIDs.size() + 1;
						//this.lastListingID = itemsAndIDs.size() + 1;
						toAddItem = new Item(seller, itemName, listingId, lowestBiddingPrice, biddingDurationMs);
						itemsAndIDs.put(listingId, toAddItem);
					}
					synchronized (itemPerBuyerSellerLock) {
						if(itemsPerSeller.containsKey(seller)){
							if(itemsPerSeller.get(seller) >= maxSellerItems)
							return -1;
							else {
								itemsPerSeller.put(seller, itemsPerSeller.get(seller) + 1);
							}
						}else{
							itemsPerSeller.put(seller,1);
						}
					}
					if(biddingDurationMs == 0){
						if(durationMap.containsKey(seller)){
						durationMap.put(seller, durationMap.get(seller) + 1);}else{
							durationMap.put(seller, 1);
						}
					}
					
					lastListingID = listingId;
					itemsUpForBidding.add(toAddItem);
					
					return lastListingID;
					
					
				}//
					
				}
			
		}
	//	return -1;
			
		
	
	}
			
	



	/**
	 * Get all <code>Items</code> active in the auction
	 * @return A copy of the <code>List</code> of <code>Items</code>
	 */
	public List<Item> getItems()
	{
		// TODO: IMPLEMENT CODE HERE
		// Some reminders:
		//    Don't forget that whatever you return is now outside of your control.
			synchronized (itemsUpForBiddingLock) {
				List<Item> currentActiveItems = new ArrayList<>();
				for(Item item : itemsUpForBidding){
					currentActiveItems.add(item);
				}
				return currentActiveItems;
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
	if(bidder.equals(null) && bidder.isEmpty() && listingID <= 0 && biddingAmount < 0){
		return false;
	}
	synchronized (itemsUpForBiddingLock) {
		if(bidderDisqualification.containsKey(bidder)){
			return false;
		}
		Item item = null;
		synchronized (itemsAndIDsLock) {
			item = itemsAndIDs.get(listingID);	
			}
		if(item == null){
			return false;
		}
		if(!itemsUpForBidding.contains(item)){
			return false;
		}
		if(itemsUpForBidding.contains(item) && item.biddingOpen() == false){
			return false;
		}
		if(itemsUpForBidding.contains(item) && item.biddingOpen()==true && item.lowestBiddingPrice() > biddingAmount){
			if(biddingViolations.containsKey(bidder)){
				if(biddingViolations.get(bidder) > 3){
					bidderDisqualification.put(bidder, true);
					return false;
				}else{
					biddingViolations.put(bidder, biddingViolations.get(bidder) + 1);
					return false;
				}
			}else{
				biddingViolations.put(bidder, 1);
				return false;
			}
			//return false;
		}
		synchronized (itemPerBuyerSellerLock) {
			if(itemsPerBuyer.containsKey(bidder)){
				if(itemsPerBuyer.get(bidder) > maxBidCount){
					return false;
				}else{
					itemsPerBuyer.put(bidder, itemsPerBuyer.get(bidder) + 1);
				}
			}else{
				itemsPerBuyer.put(bidder,1);
			}
		}
		synchronized (highestBiddersLock) {
			if(highestBidders.containsKey(listingID)){
				if(highestBidders.get(listingID).equalsIgnoreCase(bidder)){
					return false;
				}else if(highestBids.containsKey(listingID)){
					if(highestBids.get(listingID) > biddingAmount)
						return false;
					else{
						highestBids.put(listingID, biddingAmount);
						highestBidders.put(listingID, bidder);
						return true;
					}
				}
		}else{
			if(item.lowestBiddingPrice() < biddingAmount){
				highestBids.put(listingID, biddingAmount);
				highestBidders.put(listingID, bidder);
				return true;
			}
		}
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
		if(bidderName.equals(null) || listingID <= 0){
			return 3;
		}
		synchronized (itemsUpForBiddingLock) {
			Item item = null;
			synchronized (itemsAndIDsLock) {
				item = itemsAndIDs.get(listingID);
			}
			if(item != null && itemsUpForBidding.contains(item) && item.biddingOpen() == true){
				return 2;
			}
			if(item != null && itemsUpForBidding.contains(item) && item.biddingOpen() == false){
				
				synchronized (itemPerBuyerSellerLock) {
				itemsPerBuyer.put(bidderName,itemsPerBuyer.get(bidderName)-1);
				itemsPerSeller.put(item.seller(), itemsPerSeller.get(item.seller()) -1);
				}
				synchronized (itemsUpForBiddingLock) {
				if(highestBids.containsKey(listingID) && highestBidders.get(listingID).equals(bidderName)){
					synchronized (instance) {
						soldItemsCount = soldItemsCount() + 1;
						revenue = revenue() + highestBids.get(listingID);
						itemsUpForBidding.remove(item);
						return 1;
					}
				}else{
//					synchronized (instance) {
//						soldItemsCount = soldItemsCount() + 1;
//						revenue = revenue() + highestBids.get(listingID);
//						
//					}
					return 3;
				}
				}
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
		Item item = null;
		if(listingID > 0){
			synchronized (itemsUpForBiddingLock) {
				// Retrieve the item
				synchronized (itemsAndIDsLock) {
					item = itemsAndIDs.get(listingID);
				}
				if(item != null){
					if(!itemsUpForBidding.contains(item)){
						return -1;
					}
				}
				synchronized (highestBiddersLock) {
					if(highestBids.containsKey(listingID)){
						return highestBids.get(listingID);
					}else{
						return item.lowestBiddingPrice();
					}
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
		if(listingID > 0){
		synchronized (itemsUpForBiddingLock) {
			synchronized (itemsAndIDsLock) {
				item = itemsAndIDs.get(listingID);
			}
			if(item != null && !itemsUpForBidding.contains(item)){
				return true;
			}
			synchronized (highestBiddersLock) {
				if(highestBids.containsKey(listingID)){
					return true;
				}
			}
		}
		}
		return false;
	}
//	public int returnSumofHighestBids(){
//		int sum =0;
//		for(int id : highestBids.keySet()){
//			sum = sum + highestBids.get(id);
//		}
//		return sum;
//	}
//public int returnItemSize(){
//	return itemsAndIDs.size();
//}
//public int returnLastingId(){
//	return lastListingID;
//}
//public int returnHighestBidders(){
//	return highestBidders.size();
//}

}
 