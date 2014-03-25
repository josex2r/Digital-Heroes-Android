package com.josex2r.digitalheroes.model;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;
import android.util.SparseArray;

import com.josex2r.digitalheroes.controllers.FavouritesSQLiteHelper;
import com.josex2r.digitalheroes.utils.DiskLruImageCache;

public class Blog {
	//Singleton pattern
	private static Blog INSTANCE=new Blog();
	//-------------	Posts collection -------------
	private SparseArray<SparseArray<List<Post>>> posts;
	//-------------	Current filter showing -------------
	private int activeFilter;
	//-------------	Current page showing -------------
	private int currentPage;
	//-------------	�Loading posts? -------------
	private boolean loading;
	//-------------	RSS feed URL -------------
	private String feedUrl;
	//-------------	Bitmap Cache -------------
    private DiskLruImageCache images;
    
    private Context context;
    private final static int DB_VERSION=1;
	
	//-------------	Amount of post per RSS feed page -------------
	public static final int POSTS_PER_FEED=10;
	
	//-------------	Post Filters -------------
	public static final String DEFAULT_FEED_URL="http://blog.gobalo.es/feed/";
		//-------------	All -------------
		public static final int FILTER_ALL=0;
		//-------------	Categories -------------
		public static final int FILTER_ADVERSITING=1;
		public static final int FILTER_CREATIVIDAD=2;
		public static final int FILTER_INSIDE=3;
		public static final int FILTER_MARKETING=4;
		public static final int FILTER_NEGOCIOS=5;
		public static final int FILTER_SEO=6;
		public static final int FILTER_WEB=7;
		//-------------	Authors -------------
		public static final int FILTER_BINARY=20;
		public static final int FILTER_CODE=21;
		public static final int FILTER_CRAFT=22;
		public static final int FILTER_CREA=23;
		public static final int FILTER_IDEA=24;
		public static final int FILTER_NUMBERS=25;
		public static final int FILTER_PENCIL=26;
		public static final int FILTER_PIXEL=27;
		public static final int FILTER_SEM=28;
		public static final int FILTER_SOCIAL=29;
		public static final int FILTER_SPEED=30;
		public static final int FILTER_TRIX=31;
		//-------------	Favourites -------------
		public static final int FILTER_FAVOURITES=99;
	
	//-------------	Constructor -------------
	public Blog(){
		this.feedUrl=DEFAULT_FEED_URL;
		this.currentPage=1;
		this.activeFilter=Blog.FILTER_ALL;
		this.posts=new SparseArray<SparseArray<List<Post>>>();
		this.loading=false;
		//loadFavouritesFromDB();
	}
	
	private static void createInstance(){
        if( INSTANCE==null ){
            synchronized(Blog.class){
                if( INSTANCE==null ){ 
                    INSTANCE=new Blog();
                }
            }
        }
    }
	
	public static Blog getInstance(){
        createInstance();
        return INSTANCE;
    }
	
	//-------------	Getters -------------
	public int getCurrentPage(){
		return this.currentPage;
	}
	public boolean isLoading(){
		return loading;
	}
	public SparseArray<SparseArray<List<Post>>> getPosts(){
		return this.posts;
	}
	public int getActiveFilter(){
		return activeFilter;
	}
	public String getFeedUrl(){
		return feedUrl;
	}
	public int getPage(){
		return currentPage;
	}
	public DiskLruImageCache getImages(){
		return images;
	}
	
	//-------------	Setters -------------
	public void setCurrentPage(int page){
		Log.d("MyApp", "Setting context");
		this.currentPage=page;
	}
	public void setLoading(boolean l){
		this.loading=l;
	}
	public void setFeedUrl(String url){
		this.feedUrl=url;
	}
	public void setActiveFilter(int filter){
		this.activeFilter=filter;
	}
	public void setContext(Context context){
		this.context=context;
		this.images=new DiskLruImageCache(this.context, "postCache", 500000, CompressFormat.JPEG, 80); //1Mb memory cache
	}
	
	public void loadFavouritesFromDB(){
		List<Post> loadedPosts=new ArrayList<Post>();
		
		Log.d("MyApp", "is context!=null????? -> "+Boolean.toString(context!=null));
		if(context!=null){
			Log.d("MyApp", "context!=null");
			SQLiteDatabase db=null;
			try {
				Log.d("MyApp", "/********************************/");
				Log.d("MyApp", "/******* Cargar Favoritos *******/");
				Log.d("MyApp", "/********************************/");
				
				FavouritesSQLiteHelper conexionDB=new FavouritesSQLiteHelper(context, "DBFavourites", null, DB_VERSION);
				db=conexionDB.getReadableDatabase();
				Cursor i=db.rawQuery("SELECT title, link, comments, date, creator, guid, description, imageLink FROM favourites WHERE 1", null);
				
				if(i.getCount()>0){
					i.moveToFirst();
					do{
						Log.d("MyApp", "LOADED FROM BBDD: "+i.getString(0));
						Post post=new Post();
						post.setTitle( i.getString(0) );
						post.setLink( i.getString(1) );
						post.setComments( i.getString(2) );
						post.setDate( i.getString(3) );
						post.setCreator( i.getString(4) );
						post.setGuid( i.getString(5) );
						post.setDescription( i.getString(6) );
						post.setImageLink( i.getString(7) );
						
						loadedPosts.add( post );
					}while(i.moveToNext());
				}
			}catch(Exception e){
				throw new RuntimeException(e);
			}
			finally{
				db.close();	
			}
		}
		this.addPosts(Blog.FILTER_FAVOURITES, 1, loadedPosts);
	}
	
	//-------------	Add posts to posts collection -------------
	public void addPosts(int filter, int page, List<Post> postsToAdd){
		if( posts.get(filter)==null ){
			posts.put(filter, new SparseArray<List<Post>>());
		}
		//Never override pages
		if(posts.get(filter).get(page)==null)
			posts.get(filter).put(page, postsToAdd);
	}
	
	//-------------	Get all post from page 1 to current page -------------
	public List<Post> getFilteredAllPagedPosts(){
		List<Post> filteredPagedPosts=new ArrayList<Post>();
		if( this.posts.get(this.activeFilter)!=null ){
			for(int i=1; i<this.currentPage+1; i++){
				if( this.posts.get(this.activeFilter).get(i)!=null ){
					filteredPagedPosts.addAll( this.posts.get(this.activeFilter).get(i) );
				}
			}
		}
		return filteredPagedPosts;
	}
	
	//-------------	Get all post from current page -------------
	public List<Post> getFilteredPagedPosts(){
		
		if(this.activeFilter==Blog.FILTER_FAVOURITES){
			
			return this.posts.get(Blog.FILTER_FAVOURITES).get(1);
			
		}else{
			
			List<Post> filteredPagedPosts=new ArrayList<Post>();
			if( this.posts.get(this.activeFilter)!=null ){
				if( this.posts.get(this.activeFilter).get(this.currentPage)!=null ){
					filteredPagedPosts.addAll( this.posts.get(this.activeFilter).get(this.currentPage) );
				}
			}
			return filteredPagedPosts;
		
		}
		
	}
	
	public void addRemoveFromFavourites(int position){
		if(context!=null){
			Log.d("MyApp", "addRemoveFromFavourites:"+Integer.toString(position));
			
			Post selectedPost=getFilteredAllPagedPosts().get(position);
			String link=selectedPost.getLink();
			
			//Always delete
			
			
			if(!isFavourite(link)){
				
				//Insert
				removeFavourite(link);
				addFavourite(selectedPost);
				
			}else
				removeFavourite(link);
			
		}
	}
	
	private boolean removeFavourite(String link){
		Log.d("MyApp", "Eliminando de favoritos: "+link);
		if(context!=null){
			SQLiteDatabase db=null;
			try {
				FavouritesSQLiteHelper conexionDB=new FavouritesSQLiteHelper(context, "DBFavourites", null, DB_VERSION);
				db=conexionDB.getWritableDatabase();
				
				db.delete("favourites", "link=?", new String[]{link});
				
				List<Post> favourites=this.posts.get(Blog.FILTER_FAVOURITES).get(1);
				for(int i=0;i<favourites.size();i++){
					if( favourites.get(i).getLink().equals(link) )
						favourites.remove(i);
				}
				
				return true;
				
			}catch(Exception e){
				return false;
			}finally{
				db.close();	
			}
		}else
			return false;
	}
	
	private boolean addFavourite(Post post){
		Log.d("MyApp", "A�adiendo a favoritos: "+post.getTitle());
		if(context!=null){
			SQLiteDatabase db=null;
			try {
				FavouritesSQLiteHelper conexionDB=new FavouritesSQLiteHelper(context, "DBFavourites", null, DB_VERSION);
				db=conexionDB.getWritableDatabase();
				
				ContentValues insertSQL = new ContentValues();
				insertSQL.put("title", post.getTitle());
				insertSQL.put("link", post.getLink());
				insertSQL.put("comments", post.getComments());
				insertSQL.put("date", post.getDate());
				insertSQL.put("creator", post.getCreator());
				insertSQL.put("guid", post.getGuid());
				insertSQL.put("description", post.getDescription());
				insertSQL.put("imageLink", post.getImageLink());
				db.insert("favourites", null, insertSQL);
				
				this.posts.get(Blog.FILTER_FAVOURITES).get(1).add( post );
				
				return true;
				
			}catch(Exception e){
				return false;
			}finally{
				db.close();	
			}
		}else
			return false;
	}
	
	public boolean isFavourite(String link){
		boolean found=false;
		List<Post> favourites=this.posts.get(Blog.FILTER_FAVOURITES).get(1);
		for(int i=0;i<favourites.size();i++){
			if(favourites.get(i).getLink().equals(link))
				found=true;
		}
		return found;
	}
	
}
