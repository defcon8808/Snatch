package com.factorysoft.snatch;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
        protected String mDateTime; // DateTime

        public MemoCard(Context context, int id, String titleHeader, String titleMain, String rgb, String dateTime) {
            super(context, R.layout.inner_content);
            this.mId=id;
            this.mTitleHeader=titleHeader;
            this.mTitleMain=titleMain;
            this.mRgb=rgb;
            this.mDateTime=dateTime;
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
                    //Log.d("subMenu", item.getTitle().toString());

                    if(item.getTitle().equals("문자전송")) {
                        //Toast.makeText(getBaseContext(), "문자전송 다이얼로그 팝업", Toast.LENGTH_SHORT).show();
                        //int itemId = Integer.parseInt(card.getId());
                        //int itemId = item.getOrder()+1;
                        showDialog(MainActivity.this, mId);
                    } else {
                        db.execSQL("DELETE FROM memo WHERE _id='" + mId + "'");
                        AddMemo.delete = true;
                        initCards();
                    }
                }
            });

            addCardHeader(header);

            //Add ClickListener
            setOnClickListener(new OnCardClickListener() {
                @Override
                public void onClick(Card card, View view) {
                    Intent intent = new Intent(getBaseContext(), EditMemo.class);
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

            RelativeLayout sndSector = (RelativeLayout)parent.findViewById(R.id.second_sector);
            ImageView sticky_color = (ImageView)parent.findViewById(R.id.colorBorder);

            GradientDrawable changeColor = (GradientDrawable)sticky_color.getBackground();

            // Change sticky color.
            changeColor.setColor(Integer.parseInt(mRgb));

            if(mDateTime != null) {
                sndSector.setVisibility(View.VISIBLE);
                TextView tvDateTime = (TextView)parent.findViewById(R.id.tvDateTime);
                tvDateTime.setText(mDateTime);
            }

        }
    }

    /*
     * SQLite 코드로 DB 조회 후 리스트뷰에 뿌림.
     * initialize Cardss
     */
    private void initCards() {
        ArrayList<Card> cards = new ArrayList<Card>();

        Cursor cursor = db.rawQuery("SELECT _id, title, content, rgb, time FROM memo", null);

        while(cursor.moveToNext()) {
            //Log.d("initCards()", "_id : " + cursor.getInt(cursor.getColumnIndex("_id")));
            MemoCard card = new MemoCard(this, cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
            cards.add(card);
        }

        cursor.close();

        CardArrayAdapter mCardArrayAdapter = new CardArrayAdapter(this,cards);

        CardListView listView = (CardListView) findViewById(R.id.carddemo_list_base1);

        if (listView!=null){
            listView.setAdapter(mCardArrayAdapter);
        }
    }

    private void showDialog(Context context, int id) {
        AlertDialog.Builder builder;
        AlertDialog alertDialog;

        LayoutInflater inflater = (LayoutInflater)context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.custom_dialog, (ViewGroup)findViewById(R.id.root_layout));

        EditText editTitle = (EditText)view.findViewById(R.id.editTitle);
        EditText editContent = (EditText)view.findViewById(R.id.editContent);
        EditText editDateTime = (EditText)view.findViewById(R.id.editDateTime);

        Cursor cursor = db.rawQuery("SELECT title, content, time FROM memo WHERE _id='"+ id +"'", null);

        while(cursor.moveToNext()) {
            editTitle.setText(cursor.getString(cursor.getColumnIndex("title")));
            editContent.setText(cursor.getString(cursor.getColumnIndex("content")));

            if (cursor.getString(cursor.getColumnIndex("time")) != null) {
                editDateTime.setText(cursor.getString(cursor.getColumnIndex("time")));
            } else {
                editDateTime.setHint("설정안됨");
            }
        }

        cursor.close();

        builder = new AlertDialog.Builder(context);
        builder.setView(view);
        builder.setTitle("문자전송");
        builder.setPositiveButton("전송", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(getApplicationContext(), "문자 전송 완료", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        alertDialog = builder.create();
        alertDialog.show();
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
            Intent intent = new Intent(getBaseContext(), AddMemo.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}