package com.josex2r.digitalheroes.fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.josex2r.digitalheroes.LoaderActivity;
import com.josex2r.digitalheroes.MainActivity;
import com.josex2r.digitalheroes.R;
import com.josex2r.digitalheroes.controllers.AllPostsFragmentAdapter;
import com.josex2r.digitalheroes.controllers.AsyncTaskListener;
import com.josex2r.digitalheroes.controllers.RssBlogPostLoader;
import com.josex2r.digitalheroes.model.Blog;
import com.josex2r.digitalheroes.model.BlogFilter;
import com.josex2r.digitalheroes.model.Post;

import java.util.ArrayList;
import java.util.List;


public class AllPostsFragment extends Fragment implements OnItemClickListener, OnScrollListener, OnClickListener{
    //-------------	Blog model -------------
    private Blog blog;
    //-------------	ListView adapter -------------
    private AllPostsFragmentAdapter adapter;
    //-------------	ListView -------------
    private ListView lvPosts;
    //-------------	Progress bar circle -------------
    private LinearLayout lyLoader;
    //-------------	Load post on scrolling -------------
    private static final int VISIBLE_THRESHOLD = 5;
    //Context menu actions
    public static final int CONTEXT_MENU_SHOW_POST = 0;
    public static final int CONTEXT_MENU_TOGGLE_FAVOURITE = 1;

    //-------------	Constructor -------------
    public AllPostsFragment(){
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_all_posts,
                container, false);
        //-------------	Get Activity -------------
        MainActivity mainActivity = (MainActivity)getActivity();
        //-------------	Get static Blog -------------
        this.blog = Blog.getInstance();
        //-------------	Get loader from View -------------
        lyLoader = (LinearLayout)rootView.findViewById(R.id.lyLoader);
        //-------------	Set Loading state -------------
        loading(true);
        //-------------	Manage ListView -------------
        lvPosts = (ListView)rootView.findViewById(R.id.lvPosts);
        adapter = new AllPostsFragmentAdapter(getActivity(), R.layout.blog_post, new ArrayList<Post>(), lvPosts, this);
        lvPosts.setAdapter(adapter);
        lvPosts.setOnItemClickListener(this);
        lvPosts.setOnScrollListener(this);
        registerForContextMenu(lvPosts);
        //-------------	If post loaded from internet == 0, else show list -------------
        List<Post> currentPosts = blog.getFilteredAllPagedPosts();
        //Log.d("MyApp", "-------- Page: "+Integer.toString(blog.getCurrentPage()) );
        if( currentPosts==null || currentPosts.size()==0 && !blog.getActiveFilter().equals(Blog.FILTER_FAVOURITES) ){
            adapter.clear();
            loadCurrentPage();

            //Start loading screen if first time
            if( blog.getPosts().get(0)==null ){
                Intent intent = new Intent(mainActivity, LoaderActivity.class);
                startActivityForResult(intent, Blog.REQUEST_LOAD);
            }
        }else{
            blog.setCurrentPage(1);
            displayPosts();
        }
        return rootView;
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        lvPosts.setSelection(0);
        super.onResume();
    }

    //-------------	Add posts to ListView -------------
    public void displayPosts(){
        loading(true);
        AsyncTask<Integer, Integer, List<Post>> displayAsync = new AsyncTask<Integer, Integer, List<Post>>() {
            @Override
            protected List<Post> doInBackground(Integer... integers) {
                List<Post> currentPosts=blog.getFilteredPagedPosts();
                return currentPosts;
            }

            @Override
            protected void onPostExecute(List<Post> currentPosts) {
                super.onPostExecute(currentPosts);
                for(Post currentPost : currentPosts){
                    adapter.add(currentPost);
                }
                adapter.getListView().setSelection(0);
                adapter.notifyDataSetChanged();
                loading(false);
            }
        };

        displayAsync.execute(0);
    }

    //-------------	Loader actions -------------
    public void loading(boolean action){
        if(action){
            if(lyLoader!=null)
                lyLoader.setVisibility(View.VISIBLE);
            blog.setLoading(true);
        }else{
            if(lyLoader!=null)
                lyLoader.setVisibility(View.GONE);
            blog.setLoading(false);
        }
    }

    //-------------	Load posts from Internet -------------
    public void loadCurrentPage(){
        final BlogFilter currentFilter = blog.getActiveFilter();
        final int currentPage = blog.getCurrentPage();
        RssBlogPostLoader page=new RssBlogPostLoader(new AsyncTaskListener<List<Post>>() {
            @Override
            public void onTaskComplete(List<Post> loadedPosts) {
                blog.addPosts(currentFilter, currentPage, loadedPosts);
                displayPosts();
                blog.dispatchLoadListener(true);
                loading(false);
            }
            public void onTaskFailed() {
                //No more posts
                loading(false);
                //Force loading state to prevent more loadings
                blog.setLoading(true);
                blog.dispatchLoadListener(false);
            }
        });
        page.execute(blog.getCurrentPage());
    }




    private void selectPost(int position){
        Post selectedPost=adapter.getItem(position);
        StringBuilder str=new StringBuilder();
        String url=selectedPost.getLink();
        str.append(url).append("&android=true");

        Bundle data=new Bundle();
        data.putString("uri", str.toString());
        data.putString("title", selectedPost.getTitle());

        Intent i = new Intent("com.josex2r.digitalheroes.BrowserActivity");
        i.putExtras(data);
        //i.setData(Uri.parse(str.toString()));
        startActivity(i);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
        // TODO Auto-generated method stub
        //Log.d("MyApp",Integer.toString(view.getId()));
        selectPost(position);

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        // TODO Auto-generated method stub

        if( blog.getActiveFilter().equals(Blog.FILTER_FAVOURITES) ){
            return;
        }

        if (!blog.isLoading() && (totalItemCount - visibleItemCount) <= (firstVisibleItem + VISIBLE_THRESHOLD) ) {

            loading(true);

            blog.setCurrentPage(blog.getCurrentPage()+1);

            List<Post> posts=blog.getFilteredPagedPosts();

            if( posts.size()>0 ){
                //Log.d("MyApp", "Se han encontrado posts, page: "+Integer.toString(blog.getCurrentPage()));
                displayPosts();
                loading(false);
            }else{
                //Log.d("MyApp", "No se han encontrado posts");
                loadCurrentPage();
            }

            //blog.nextPage();
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {}


    private void toggleFavourite(int position){
        blog.addRemoveFromFavourites( position );
        //If favourites filter, reload the data set
        if( blog.getActiveFilter().equals(Blog.FILTER_FAVOURITES) ){
            adapter.clear();
            adapter.addAll(blog.getFilteredPagedPosts());
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        Integer position=(Integer) v.getTag();
        toggleFavourite( position );
    }




    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
        // TODO Auto-generated method stub
        if( v.getId()==R.id.lvPosts ) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            String title = blog.getFilteredAllPagedPosts().get(info.position).getTitle();
            menu.setHeaderTitle(title);
            menu.add(Menu.NONE, CONTEXT_MENU_SHOW_POST, Menu.NONE, getResources().getString(R.string.context_menu_show));
            menu.add(Menu.NONE, CONTEXT_MENU_TOGGLE_FAVOURITE, Menu.NONE, getResources().getString(R.string.context_menu_toggle_favourites));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch( item.getItemId() ){
            case CONTEXT_MENU_SHOW_POST:
                selectPost(info.position);
                return true;
            case CONTEXT_MENU_TOGGLE_FAVOURITE:
                toggleFavourite( info.position );
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }


}