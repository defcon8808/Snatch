package com.factorysoft.snatch;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.internal.CardHeader;
import it.gmariotti.cardslib.library.internal.base.BaseCard;
import it.gmariotti.cardslib.library.view.CardListView;


public class MainActivity extends FragmentActivity {
    private static SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DBHelper dbmanager = new DBHelper(this);

        try {
            db = dbmanager.getWritableDatabase();
        } catch (SQLiteException e) {
            db = dbmanager.getReadableDatabase();
        }

        this.setTitle("메모");
        initCards();
    }

    @Override
    protected void onStart() {
        super.onStart();
        initCards();
    }

    public class MemoCard extends Card{

        protected String mTitleHeader; // Main Title
        protected String mTitleMain; // Content
        protected String mRgb; // Color code
        protected int mId; // Column ID

        public MemoCard(Context context, int id, String titleHeader, String titleMain, String rgb) {
            super(context, R.layout.inner_content);
            this.mId=id;
            this.mTitleHeader=titleHeader;
            this.mTitleMain=titleMain;
            this.mRgb=rgb;
            init();
        }

        private void init(){

            //Create a CardHeader
            CardHeader header = new CardHeader(getBaseContext());

            //Set the header title
            header.setTitle(mTitleHeader);

            //Add a popup menu. This method set OverFlow button to visible
            header.setPopupMenu(R.menu.edit_menu, new CardHeader.OnClickCardHeaderPopupMenuListener() {
                @Override
                public void onMenuItemClick(BaseCard card, MenuItem item) {
                    Log.d("subMenu", item.getTitle().toString());

                    if(item.getTitle().equals("문자전송")) {
                        Toast.makeText(getContext(), "문자전송 다이얼로그 팝업", Toast.LENGTH_SHORT).show();
                    } else {
                        db.execSQL("DELETE FROM memo WHERE _id='" + mId + "'");
                        initCards();
                    }
                }
            });

            addCardHeader(header);

            //Add ClickListener
            setOnClickListener(new OnCardClickListener() {
                @Override
                public void onClick(Card card, View view) {
                    Intent intent = new Intent(getContext(), EditMemo.class);
                    intent.putExtra("id", mId);
                    startActivity(intent);
                }
            });

            //Set the card inner text
            this.setTitle(mTitleMain);
        }

        /*
            InnerViewElements Modification.
         */
        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {
            super.setupInnerViewElements(parent, view);

            ImageView sticky_color = (ImageView)parent.findViewById(R.id.colorBorder);

            GradientDrawable changeColor = (GradientDrawable)sticky_color.getBackground();

            // Change sticky color.
            changeColor.setColor(Integer.parseInt(mRgb));
        }
    }

    /*
     * SQLite 코드로 DB 조회 후 리스트뷰에 뿌림.
     * initialize Cards
     */
    private void initCards() {
        ArrayList<Card> cards = new ArrayList<Card>();

        Cursor cursor = db.rawQuery("SELECT _id, title, content, rgb FROM memo", null);

        while(cursor.moveToNext()) {
            MemoCard card = new MemoCard(this, cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
            cards.add(card);
        }

        cursor.close();

        CardArrayAdapter mCardArrayAdapter = new CardArrayAdapter(this,cards);

        CardListView listView = (CardListView) findViewById(R.id.carddemo_list_base1);

        if (listView!=null){
            listView.setAdapter(mCardArrayAdapter);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_add) {
            Intent intent = new Intent(this, AddMemo.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}