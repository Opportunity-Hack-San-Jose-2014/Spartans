package info.androidhive.slidingmenu;

import java.math.BigDecimal;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.opengl.Visibility;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PaymentActivity;
 
public class ViewPagerAdapter extends PagerAdapter {
    // Declare Variables
    Context context;
    DetailsBean[] details;
    int[] flag;
    LayoutInflater inflater;
    private ImageLoader _imageLoader;
    private DisplayImageOptions _options;
 
    public ViewPagerAdapter(Context context, DetailsBean[] details, ImageLoader imgLoader,DisplayImageOptions options) {
        this.context = context;
        this.details = details;
        this._imageLoader = imgLoader;
        this._options = options;
    }
 
    @Override
    public int getCount() {
        return details==null ? 0: details.length;
    }
 
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }
 
    @Override
    public Object instantiateItem(ViewGroup container, final int position) {
 
        // Declare Variables
    	TextView studentName;
        TextView grade ;
        TextView performance ;
        TextView size ;
        TextView occupation ;
        TextView mother ;
        TextView donation ;
        ImageView imgView;
        TextView redText;
        TextView desc;
        
        TextView textView1;
        TextView textView2 ;
        TextView textView3 ;
        TextView textView4 ;
        TextView textView5 ;
        TextView textView6 ;
        TextView textView7 ;
        TextView textView8;
        
        
        
 
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.viewpager_item, container,
                false);
        
        Typeface custom_font3 = Typeface.createFromAsset(this.context.getAssets(),
                "fonts/quicksandr.ttf");
        
        textView1 = (TextView) itemView.findViewById(R.id.textView1);
        //studentName.setText(details[position].getApplicant());
        textView1.setTypeface(custom_font3);
        
        textView2 = (TextView) itemView.findViewById(R.id.textView2);
        //grade.setText(details[position].getDegree());
        textView2.setTypeface(custom_font3);
        
        textView3 = (TextView) itemView.findViewById(R.id.textView3);
        //performance.setText(details[position].getMerit());
        textView3.setTypeface(custom_font3);
        
        textView4 = (TextView) itemView.findViewById(R.id.textView4);
        //size.setText(details[position].getAwards());
        textView4.setTypeface(custom_font3);
        
        textView5 = (TextView) itemView.findViewById(R.id.textView5);
       // occupation.setText(details[position].getOccupation());
        textView5.setTypeface(custom_font3);
        
        textView6 = (TextView) itemView.findViewById(R.id.textView6);
       // mother.setText(details[position].getExtracurricular());
        textView6.setTypeface(custom_font3);
        
        textView7 = (TextView) itemView.findViewById(R.id.textView7);
       
        textView7.setTypeface(custom_font3);
                 
        
        //textView8 = (TextView) itemView.findViewById(R.id.textView8);
        
       // textView8.setTypeface(custom_font3);
         
         
        
        Button buyBtn = (Button) itemView.findViewById(R.id.donateButton);
        Button fbuyBtn = (Button) itemView.findViewById(R.id.fshareButton);
        
         studentName = (TextView) itemView.findViewById(R.id.studentName);
         studentName.setText(details[position].getApplicant());
         studentName.setTypeface(custom_font3);
         
         grade = (TextView) itemView.findViewById(R.id.grade);
         grade.setText(details[position].getDegree());
         grade.setTypeface(custom_font3);
         
         performance = (TextView) itemView.findViewById(R.id.performance);
         performance.setText(details[position].getMerit());
         performance.setTypeface(custom_font3);
         
         size = (TextView) itemView.findViewById(R.id.size);
         size.setText(details[position].getAwards());
         size.setTypeface(custom_font3);
         
         occupation = (TextView) itemView.findViewById(R.id.occupation);
         occupation.setText(details[position].getOccupation());
         occupation.setTypeface(custom_font3);
         
         mother = (TextView) itemView.findViewById(R.id.mother);
         mother.setText(details[position].getExtracurricular());
         mother.setTypeface(custom_font3);
         
         donation = (TextView) itemView.findViewById(R.id.donation);
        donation.setText(details[position].getAmountsought());
        donation.setTypeface(custom_font3);
        
        redText = (TextView) itemView.findViewById(R.id.redText);
        
        if (details[position].getMatching().equals("n"))
        	redText.setVisibility(View.GONE);
        else{
        	String[] offset = details[position].getOffset().split("-");
        	redText.setText(details[position].getDescription().substring(Integer.parseInt(offset[0]), Integer.parseInt(offset[1]))+"...");
        	//Typeface custom_font = Typeface.createFromAsset(this.context.getAssets(),
              //      "fonts/dancingscript.ttf");
                    Typeface custom_font1 = Typeface.createFromAsset(this.context.getAssets(),
                           "fonts/caviardreamsbold.ttf");
                            
                    
                    redText.setTypeface(custom_font1,Typeface.ITALIC);
        }
        
        
        
        desc = (TextView) itemView.findViewById(R.id.desc);
        desc.setText(details[position].getDescription());
        desc.setTypeface(custom_font3);
        
        fbuyBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MainActivity.photoFragment.shareToWall();
				
			}
		});
        
        
        buyBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MainActivity.photoFragment.onBuyPressed(details[position].getAmountsought(), details[position].getId());
				
			}
		});
 
        // Locate the TextViews in viewpager_item.xml
 
        // Capture position and set to the TextViews
        //txtrank.setText(rank[position]);
        //txtcountry.setText(country[position]);
      //  txtpopulation.setText(population[position]);
 
        // Locate the ImageView in viewpager_item.xml
        imgView = (ImageView) itemView.findViewById(R.id.imageView1);
        // Capture position and set to the ImageView
        _imageLoader.displayImage(details[position].getPhotoURL(), imgView, _options, new SimpleImageLoadingListener() {
			@Override
			public void onLoadingStarted(String imageUri, View view) {
				//spinner.setVisibility(View.VISIBLE);
			}

			@Override
			public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
				String message = null;
				switch (failReason.getType()) {
					case IO_ERROR:
						message = "Input/Output error";
						break;
					case DECODING_ERROR:
						message = "Image can't be decoded";
						break;
					case NETWORK_DENIED:
						message = "Downloads are denied";
						break;
					case OUT_OF_MEMORY:
						message = "Out Of Memory error";
						break;
					case UNKNOWN:
						message = "Unknown error";
						break;
				}
				Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

				//spinner.setVisibility(View.GONE);
			}

			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				//spinner.setVisibility(View.GONE);
			}
		});
 
        // Add viewpager_item.xml to ViewPager
        ((ViewPager) container).addView(itemView);
 
        return itemView;
    }
    
    
 
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        // Remove viewpager_item.xml from ViewPager
        ((ViewPager) container).removeView((LinearLayout) object);
 
    }
}
