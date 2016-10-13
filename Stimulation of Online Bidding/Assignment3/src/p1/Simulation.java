package p1;


/**
 * Class provided for ease of test. This will not be used in the project 
 * evaluation, so feel free to modify it as you like.
 */ 
public class Simulation
{
    public static void main(String[] args)
    {                
        int nrSellers = 50;
        int nrBidders = 20;
        
        Thread[] sellerThreads = new Thread[nrSellers];
        Thread[] bidderThreads = new Thread[nrBidders];
        Seller[] sellers = new Seller[nrSellers];
        Bidder[] bidders = new Bidder[nrBidders];
        
        // Start the sellers
        for (int i=0; i<nrSellers; ++i)
        {
            sellers[i] = new Seller(
            		AuctionServer.getInstance(), 
            		"Seller"+i, 
            		100, 50, i
            );
            sellerThreads[i] = new Thread(sellers[i]);
            sellerThreads[i].start();
        }
        
        // Start the buyers
        for (int i=0; i<nrBidders; ++i)
        {
            bidders[i] = new Bidder(
            		AuctionServer.getInstance(), 
            		"Buyer"+i, 
            		1000, 20, 150, i
            );
            bidderThreads[i] = new Thread(bidders[i]);
            bidderThreads[i].start();
        }
        
        // Join on the sellers
        for (int i=0; i<nrSellers; ++i)
        {
            try
            {
                sellerThreads[i].join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        
        // Join on the bidders
        for (int i=0; i<nrBidders; ++i)
        {
            try
            {
                sellerThreads[i].join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
//        AuctionServer server = AuctionServer.getInstance();
//        int bidderAmount =0;
//        for(Bidder b : bidders){
//        	bidderAmount = bidderAmount + b.cashSpent();
//        }
//        int totalitems =0;
//        for(Seller s : sellers){
//        	totalitems = totalitems + s.getIntlist().size();
//        }
//        System.out.println(bidderAmount);
//        System.out.println(server.revenue());
//        System.out.println(server.returnSumofHighestBids());
//        System.out.println(server.returnLastingId());
//        System.out.println(server.returnItemSize());
//        System.out.println(server.returnHighestBidders());
//        System.out.println(server.soldItemsCount());
//        System.out.println(server.returnItemSize());
//        System.out.println(totalitems);
   System.out.println("Hello");
        // TODO: Add code as needed to debug
        
    }
}