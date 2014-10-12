package info.androidhive.slidingmenu;

import java.math.BigDecimal;

import org.json.JSONException;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

public class PhotosFragment extends Fragment {
	
	private Context ctx;
	private ViewPagerAdapter adapter;
	private String[] rank;
	private String[] country;
	private String[] population;
	private String[] images = {"http://picbook.in/wp-content/uploads/2014/07/happy_diwali__sms_images_.jpg","http://picbook.in/wp-content/uploads/2014/10/volcanoes-and-earthquakes-1.jpg"};
	private int[] flag;
	private DisplayImageOptions options;
	private DetailsBean[] details;
	public PhotosFragment(Context ctx, DetailsBean[] details){
		
		this.ctx= ctx;
		this.details = details;
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        View rootView = inflater.inflate(R.layout.fragment_photos, container, false);
        ViewPager pager = (ViewPager) rootView.findViewById(R.id.pager);
        
        options = new DisplayImageOptions.Builder()
		.showImageForEmptyUri(R.drawable.loader)
		.showImageOnFail(R.drawable.ic_error)
		.resetViewBeforeLoading(true)
		.cacheOnDisc(true)
		.imageScaleType(ImageScaleType.EXACTLY)
		.bitmapConfig(Bitmap.Config.RGB_565)
		.considerExifParams(true)
		.displayer(new FadeInBitmapDisplayer(300))
		.build();
        
        adapter = new ViewPagerAdapter(ctx,  details, ImageLoader.getInstance(),options);
        pager.setAdapter(adapter);
        //MainPagerAdapter adapter = new MainPagerAdapter(getChildFragmentManager());
        //adapter.button = getArguments().getInt("pos");
        
        
         
        return rootView;
    }
	
	public void onBuyPressed(String amnt, String id) {
        /* 
         * PAYMENT_INTENT_SALE will cause the payment to complete immediately.
         * Change PAYMENT_INTENT_SALE to 
         *   - PAYMENT_INTENT_AUTHORIZE to only authorize payment and capture funds later.
         *   - PAYMENT_INTENT_ORDER to create a payment for authorization and capture
         *     later via calls from your server.
         * 
         * Also, to include additional payment details and an item list, see getStuffToBuy() below.
         */
        PayPalPayment thingToBuy = getThingToBuy(PayPalPayment.PAYMENT_INTENT_SALE, amnt, id);

        /*
         * See getStuffToBuy(..) for examples of some available payment options.
         */

        Intent intent = new Intent(this.ctx, PaymentActivity.class);

        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);

        startActivityForResult(intent, 1);
    }
    
    private PayPalPayment getThingToBuy(String paymentIntent, String amnt, String id) {
        return new PayPalPayment(new BigDecimal(amnt), "USD", id,
                paymentIntent);
    }
    
    private String fbTitle = "hello title here";
	private String fbImage;
	private String fbUrl;
    
    void shareToWall(){
		try{
			if(fbImage == null || fbImage == "")
				fbImage = "http://www.twocentsofhope.com/images/stories/children.JPG";
			if(fbUrl == null || fbUrl == "")
				fbUrl = "http://www.twocentsofhope.com/";
			Log.d("MyFunc", "Before posting");
			Bundle params = new Bundle();
			params.putString("name","Two Cents of Hope" );
			params.putString("caption", "Realizing Dreams Through Education");
			params.putString("description", "The vision of 'Two Cents of Hope' (TCH) is to create a responsible and self-sustaining society. We hope to achieve this by facilitating education to the aspiring children and youth, which creates opportunities to realize their potential and provide them with a spring board to scale new heights");
			params.putString("link", fbUrl);
			params.putString("picture", fbImage);
			params.putString("message", "Please check this profile");
			Log.d("Myfunc", "Params Created");
			WebDialog feedDialog = (
					new WebDialog.FeedDialogBuilder(this.ctx,
							//Session.getActiveSession(),
							LoginActivity.facebook.getSession(),
							params))
							.setOnCompleteListener(new OnCompleteListener() {
								public void onComplete(Bundle values,
										FacebookException error) {
									if (error == null) {
										// When the story is posted, echo the success
										// and the post Id.
										final String postId = values.getString("post_id");
										if (postId != null) {
											Toast.makeText(ctx,"Successfully posted to your wall." ,Toast.LENGTH_SHORT).show();
											Log.d("Myfunc", "Post Id= " + postId);
										} else {
											// User clicked the Cancel button
											Toast.makeText(ctx,"Share cancelled",Toast.LENGTH_SHORT).show();
											Log.d("Myfunc", "Post Cancelled");
										}
									} else if (error instanceof FacebookOperationCanceledException) {
										// User clicked the "x" button
										Toast.makeText(ctx, "Share cancelled",Toast.LENGTH_SHORT).show();
										Log.d("Myfunc", "User Closed the Dialog");
									} else {
										// Generic, ex: network error
										Toast.makeText(ctx,"Share failed.",Toast.LENGTH_SHORT).show();
									}
								}
							}).build();
			feedDialog.show();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e("MyFunc", e.toString());
		}
	}
    
    
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                PaymentConfirmation confirm =
                        data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirm != null) {
                    try {
                        Log.i("paypal", confirm.toJSONObject().toString(4));
                        Log.i("paypal", confirm.getPayment().toJSONObject().toString(4));
                        /**
                         *  TODO: send 'confirm' (and possibly confirm.getPayment() to your server for verification
                         * or consent completion.
                         * See https://developer.paypal.com/webapps/developer/docs/integration/mobile/verify-mobile-payment/
                         * for more details.
                         *
                         * For sample mobile backend interactions, see
                         * https://github.com/paypal/rest-api-sdk-python/tree/master/samples/mobile_backend
                         */
                        Toast.makeText(
                                this.ctx,
                                "PaymentConfirmation info received from PayPal", Toast.LENGTH_LONG)
                                .show();

                    } catch (JSONException e) {
                        Log.e("paypal", "an extremely unlikely failure occurred: ", e);
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Log.i("paypal", "The user canceled.");
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Log.i(
                		"paypal",
                        "An invalid Payment or PayPalConfiguration was submitted. Please see the docs.");
            }
        }
    }
	
}
