package im.mdp.coffeecloner;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.pdf417.PDF417Writer;
import com.google.zxing.pdf417.encoder.Dimensions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;


public class CoffeeCardActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coffee_card);
        showCurrentCard();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.coffee_card, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            getScan();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void getScan(){
        IntentIntegrator integrator = new IntentIntegrator(this);
        Collection<String> formats = new ArrayList<String>(Arrays.asList("PDF_417"));
        integrator.initiateScan(formats);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            Log.d("Coffee", "Isn't null");
            String contents = scanResult.getContents();
            Toast.makeText(getApplicationContext(), contents, Toast.LENGTH_LONG);
            if (contents != null && contents.length() > 0) {
                setCurrentCard(contents);
            }
        }
    }

    protected void showPDF417(String code) {
        PDF417Writer writer = new PDF417Writer();
        Bitmap mBitmap = null;
        int width = 1500;
        int height = 1500;
        try {
            Map<EncodeHintType, Object> hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            Dimensions dimensions = new Dimensions(3,3,8,8);
            hints.put(EncodeHintType.PDF417_COMPACT, true);
            hints.put(EncodeHintType.PDF417_DIMENSIONS, dimensions);
            BitMatrix bitMatrix = writer.encode(code, BarcodeFormat.PDF_417, width, height, hints);
            width = bitMatrix.getWidth();
            height = bitMatrix.getHeight();
            Log.d("Coffee", "w: " + String.valueOf(width));
            Log.d("Coffee", "h:" + String.valueOf(height));
            mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    mBitmap.setPixel(j, i, bitMatrix.get(j, i) ? Color.BLACK : Color.WHITE);
                }
            }

        } catch (WriterException e) {
            e.printStackTrace();
        }
        if (mBitmap != null) {
            int cropBy = (int) Math.round(mBitmap.getWidth() * 0.25);
            Bitmap cropped = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth() - cropBy, mBitmap.getHeight());
            // drawCard(cropped);
            ImageView imageBarcode = (ImageView) findViewById(R.id.imageBarcode);
            imageBarcode.setImageBitmap(cropped);
        }

    }

    private void setCurrentCard(String str) {
        SharedPreferences.Editor cardPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
        cardPref.putString("cardNumber", str);
        cardPref.commit();
        showPDF417(str);
    }

    private void showCurrentCard() {
        String cardNum = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("cardNumber", "");
        if (cardNum.length() > 0) {
            showPDF417(cardNum);
        }
    }

}
