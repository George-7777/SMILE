package com.example.smile;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by AndyRoid on 5/23/15.
 */
public class QuoteBank {

    private Context mContext;

    public QuoteBank(Context context) {
        this.mContext = context;
    }

    public String readLine(String path) {
        StringBuilder mLines = new StringBuilder();

        AssetManager am = mContext.getAssets();

        try {
            InputStream is = am.open(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;

            while ((line = reader.readLine()) != null)
                mLines.append(line);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mLines.toString();
    }
}