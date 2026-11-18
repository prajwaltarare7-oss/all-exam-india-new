package in.prajwaltarare7.allexamsindia;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MainActivity extends Activity {
    private static final String HOME_URL =
            "https://prajwaltarare7-oss.github.io/all-exam-india/";
    private static final double MINIMUM_AGE = 17.5;
    private static final String PREFS = "all_exam_india_profile";
    private static final String UPDATE_JSON = HOME_URL + "app-update.json";
    private static final String WEBSITE_DOWNLOAD_PAGE = HOME_URL + "?app-update=1";

    private FrameLayout root;
    private FrameLayout webContainer;
    private WebView webView;
    private ProgressBar progressBar;
    private View errorView;
    private SharedPreferences preferences;
    private boolean showingError = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getSharedPreferences(PREFS, MODE_PRIVATE);

        getWindow().setStatusBarColor(Color.rgb(16, 27, 53));
        getWindow().setNavigationBarColor(Color.rgb(16, 27, 53));

        root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(16, 27, 53));
        setContentView(root);

        // Check the published minimum app version before opening the app.
        checkForMandatoryUpdate();
    }

    private void checkForMandatoryUpdate() {
        new Thread(() -> {
            int latestVersion = -1;
            String updateUrl = WEBSITE_DOWNLOAD_PAGE;
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(UPDATE_JSON + "?t=" + System.currentTimeMillis()).openConnection();
                connection.setConnectTimeout(7000);
                connection.setReadTimeout(7000);
                connection.setRequestProperty("Cache-Control", "no-cache");
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                StringBuilder body = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) body.append(line);
                reader.close();
                JSONObject data = new JSONObject(body.toString());
                latestVersion = data.optInt("latestVersionCode", -1);
                updateUrl = data.optString("websiteUrl", WEBSITE_DOWNLOAD_PAGE);
            } catch (Exception ignored) {
                // If update service is unreachable, allow app to open.
            } finally {
                if (connection != null) connection.disconnect();
            }
            final int remoteVersion = latestVersion;
            final String finalUpdateUrl = updateUrl;
            runOnUiThread(() -> {
                int installedVersion = 1;
                try { installedVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode; }
                catch (Exception ignored) {}
                if (remoteVersion > installedVersion) showMandatoryUpdateDialog(finalUpdateUrl, remoteVersion);
                else showMainApp();
            });
        }).start();
    }

    private void showMandatoryUpdateDialog(String updateUrl, int latestVersion) {
        new AlertDialog.Builder(this)
                .setTitle("All Exam India Update आवश्यक")
                .setMessage("या अॅपची नवीन आवृत्ती उपलब्ध आहे. अॅप वापरण्यासाठी अपडेट डाउनलोड करा.\n\nनवीन आवृत्ती: " + latestVersion)
                .setCancelable(false)
                .setPositiveButton("Update", (dialog, which) -> {
                    try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl))); }
                    catch (Exception e) { Toast.makeText(this, "Website उघडता आली नाही.", Toast.LENGTH_LONG).show(); }
                })
                .show();
    }

    private void showRegistrationScreen() {
        root.removeAllViews();

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(16, 27, 53));

        LinearLayout outer = new LinearLayout(this);
        outer.setOrientation(LinearLayout.VERTICAL);
        outer.setGravity(Gravity.CENTER);
        outer.setPadding(dp(22), dp(28), dp(22), dp(28));

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        card.setPadding(dp(22), dp(22), dp(22), dp(24));
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.WHITE);
        cardBg.setCornerRadius(dp(22));
        card.setBackground(cardBg);

        ImageView logo = new ImageView(this);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        logo.setImageResource(R.drawable.app_logo);
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(dp(172), dp(185));
        logoParams.bottomMargin = dp(8);
        card.addView(logo, logoParams);

        TextView heading = new TextView(this);
        heading.setText("Welcome to SSC EXAM.IN");
        heading.setTextColor(Color.rgb(23, 35, 61));
        heading.setTextSize(22);
        heading.setGravity(Gravity.CENTER);
        heading.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(heading, matchWrap());

        TextView intro = new TextView(this);
        intro.setText("App सुरू करण्यासाठी तुमची माहिती भरा");
        intro.setTextColor(Color.rgb(104, 116, 139));
        intro.setTextSize(14);
        intro.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams introParams = matchWrap();
        introParams.topMargin = dp(7);
        introParams.bottomMargin = dp(18);
        card.addView(intro, introParams);

        addFieldLabel(card, "नाव / Nickname");
        EditText nameInput = makeInput("तुमचे नाव लिहा",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        nameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        card.addView(nameInput, fieldParams());

        addFieldLabel(card, "वय (वर्षांत)");
        EditText ageInput = makeInput("उदा. 17.5",
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        card.addView(ageInput, fieldParams());

        TextView ageNote = new TextView(this);
        ageNote.setText("किमान वय: 17.5 वर्षे (17 वर्षे 6 महिने)");
        ageNote.setTextColor(Color.rgb(37, 99, 235));
        ageNote.setTextSize(13);
        LinearLayout.LayoutParams ageNoteParams = matchWrap();
        ageNoteParams.topMargin = dp(4);
        card.addView(ageNote, ageNoteParams);

        Button continueButton = new Button(this);
        continueButton.setText("Continue");
        continueButton.setTextColor(Color.WHITE);
        continueButton.setTextSize(16);
        continueButton.setAllCaps(false);
        continueButton.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(49, 94, 232)));
        LinearLayout.LayoutParams continueParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(52));
        continueParams.topMargin = dp(20);
        card.addView(continueButton, continueParams);

        TextView privacyNote = new TextView(this);
        privacyNote.setText("नाव आणि वय फक्त या फोनमध्ये सेव्ह राहील.");
        privacyNote.setTextColor(Color.rgb(120, 130, 148));
        privacyNote.setTextSize(12);
        privacyNote.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams privacyParams = matchWrap();
        privacyParams.topMargin = dp(12);
        card.addView(privacyNote, privacyParams);

        continueButton.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String ageText = ageInput.getText().toString().trim();
            if (name.isEmpty()) {
                nameInput.setError("नाव लिहा");
                nameInput.requestFocus();
                return;
            }
            double age;
            try {
                age = Double.parseDouble(ageText);
            } catch (Exception e) {
                ageInput.setError("वय 17.5 अशा स्वरूपात लिहा");
                ageInput.requestFocus();
                return;
            }
            if (age < MINIMUM_AGE || age > 120) {
                ageInput.setError("किमान वय 17.5 वर्षे असणे आवश्यक आहे");
                ageInput.requestFocus();
                Toast.makeText(this, "या अॅपसाठी किमान वय 17.5 वर्षे आहे.", Toast.LENGTH_LONG).show();
                return;
            }

            preferences.edit()
                    .putString("name", name)
                    .putString("age", ageText)
                    .putBoolean("profile_complete", true)
                    .apply();
            showMainApp();
        });

        outer.addView(card, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        scroll.addView(outer, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        root.addView(scroll, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void showMainApp() {
        root.removeAllViews();

        LinearLayout shell = new LinearLayout(this);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setBackgroundColor(Color.WHITE);

        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setPadding(dp(16), 0, dp(10), 0);
        toolbar.setBackgroundColor(Color.rgb(16, 27, 53));

        TextView brand = new TextView(this);
        brand.setText("ALL EXAM INDIA");
        brand.setTextColor(Color.WHITE);
        brand.setTextSize(17);
        brand.setTypeface(null, android.graphics.Typeface.BOLD);
        toolbar.addView(brand, new LinearLayout.LayoutParams(0, dp(54), 1));

        Button profileButton = new Button(this);
        profileButton.setText("👤 Profile");
        profileButton.setTextColor(Color.WHITE);
        profileButton.setTextSize(13);
        profileButton.setAllCaps(false);
        profileButton.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(49, 94, 232)));
        profileButton.setOnClickListener(v -> showProfileDialog());
        toolbar.addView(profileButton, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(42)));

        shell.addView(toolbar, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(54)));

        webContainer = new FrameLayout(this);
        shell.addView(webContainer, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        root.addView(shell, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        initializeWebView();
        if (webView.getParent() != null) {
            ((ViewGroup) webView.getParent()).removeView(webView);
        }
        webContainer.addView(webView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgressTintList(ColorStateList.valueOf(Color.rgb(37, 99, 235)));
        FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(3), Gravity.TOP);
        webContainer.addView(progressBar, progressParams);

        if (webView.getUrl() == null) {
            webView.loadUrl(HOME_URL);
        }
    }

    private void initializeWebView() {
        if (webView != null) return;

        webView = new WebView(this);
        webView.setBackgroundColor(Color.WHITE);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(false);
        settings.setMediaPlaybackRequiresUserGesture(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleUrl(request.getUrl().toString());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrl(url);
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                showingError = false;
                if (errorView != null && webContainer != null) {
                    webContainer.removeView(errorView);
                    errorView = null;
                }
                if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                applyProfileToWebsite();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request,
                                        WebResourceError error) {
                if (request.isForMainFrame()) {
                    showConnectionError();
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (progressBar != null) {
                    progressBar.setProgress(newProgress);
                    progressBar.setVisibility(newProgress >= 100 ? View.GONE : View.VISIBLE);
                }
            }
        });

        webView.setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "हा डाउनलोड उघडण्यासाठी अॅप उपलब्ध नाही.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void applyProfileToWebsite() {
        if (webView == null) return;
        String name = preferences.getString("name", "");
        String script = "(function(){try{"
                + "function hideAppDownload(){document.querySelectorAll('a.download-app-nav,a[href*=\\\"/releases/latest/download/\\\"],a[download]').forEach(function(el){el.remove();});}"
                + "hideAppDownload();"
                + "if(!window.__aeiHideDownloadObserver){window.__aeiHideDownloadObserver=new MutationObserver(hideAppDownload);window.__aeiHideDownloadObserver.observe(document.documentElement,{childList:true,subtree:true});}"
                + (name.isEmpty() ? "" : "var n=" + JSONObject.quote(name) + ";"
                + "localStorage.setItem('aei-student',JSON.stringify({name:n}));"
                + "var input=document.getElementById('regName');if(input)input.value=n;"
                + "var modal=document.getElementById('registerModal');if(modal)modal.style.display='none';"
                + "if(typeof renderStudentStats==='function')renderStudentStats();")
                + "}catch(e){}})();";
        webView.evaluateJavascript(script, null);
    }

    private void showProfileDialog() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(20), dp(8), dp(20), dp(4));

        addFieldLabel(form, "नाव / Nickname");
        EditText nameInput = makeInput("तुमचे नाव लिहा",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        nameInput.setText(preferences.getString("name", ""));
        nameInput.setFilters(new InputFilter[]{new InputFilter.LengthFilter(30)});
        form.addView(nameInput, fieldParams());

        addFieldLabel(form, "वय (वर्षांत)");
        EditText ageInput = makeInput("उदा. 17.5",
                InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        ageInput.setText(preferences.getString("age", ""));
        form.addView(ageInput, fieldParams());

        TextView note = new TextView(this);
        note.setText("किमान वय: 17.5 वर्षे (17 वर्षे 6 महिने)");
        note.setTextColor(Color.rgb(37, 99, 235));
        note.setTextSize(13);
        form.addView(note, matchWrap());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("तुमचे Profile")
                .setView(form)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Save", null)
                .create();

        dialog.setOnShowListener(d -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String ageText = ageInput.getText().toString().trim();
            if (name.isEmpty()) {
                nameInput.setError("नाव लिहा");
                return;
            }
            double age;
            try {
                age = Double.parseDouble(ageText);
            } catch (Exception e) {
                ageInput.setError("वय 17.5 अशा स्वरूपात लिहा");
                return;
            }
            if (age < MINIMUM_AGE || age > 120) {
                ageInput.setError("किमान वय 17.5 वर्षे असणे आवश्यक आहे");
                Toast.makeText(this, "किमान वय 17.5 वर्षे आहे.", Toast.LENGTH_LONG).show();
                return;
            }

            preferences.edit().putString("name", name).putString("age", ageText).apply();
            applyProfileToWebsite();
            dialog.dismiss();
            Toast.makeText(this, "Profile सेव्ह झाले.", Toast.LENGTH_SHORT).show();
        }));
        dialog.show();
    }

    private void addFieldLabel(LinearLayout parent, String value) {
        TextView label = new TextView(this);
        label.setText(value);
        label.setTextColor(Color.rgb(35, 48, 72));
        label.setTextSize(14);
        label.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams params = matchWrap();
        params.topMargin = dp(9);
        params.bottomMargin = dp(4);
        parent.addView(label, params);
    }

    private EditText makeInput(String hint, int inputType) {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setHint(hint);
        input.setInputType(inputType);
        input.setTextSize(15);
        input.setTextColor(Color.rgb(23, 35, 61));
        input.setHintTextColor(Color.rgb(145, 153, 168));
        input.setPadding(dp(13), dp(10), dp(13), dp(10));
        input.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(190, 201, 219)));
        return input;
    }

    private LinearLayout.LayoutParams fieldParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(50));
        params.bottomMargin = dp(5);
        return params;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private boolean handleUrl(String url) {
        Uri uri = Uri.parse(url);
        String scheme = uri.getScheme() == null ? "" : uri.getScheme();

        if ("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme)) {
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase();
            if (host.equals("prajwaltarare7-oss.github.io")) {
                return false;
            }
        }

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "ही लिंक उघडता आली नाही.", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private void showConnectionError() {
        if (showingError || webContainer == null) return;
        showingError = true;

        TextView message = new TextView(this);
        message.setText("इंटरनेट कनेक्शन तपासा आणि पुन्हा प्रयत्न करा.\n\nAll Exam India");
        message.setTextColor(Color.WHITE);
        message.setTextSize(18);
        message.setGravity(Gravity.CENTER);
        message.setPadding(dp(28), dp(24), dp(28), dp(24));

        Button retry = new Button(this);
        retry.setText("पुन्हा प्रयत्न करा");
        retry.setTextColor(Color.WHITE);
        retry.setAllCaps(false);
        retry.setBackgroundTintList(ColorStateList.valueOf(Color.rgb(37, 99, 235)));
        retry.setOnClickListener(v -> {
            showingError = false;
            if (errorView != null) webContainer.removeView(errorView);
            errorView = null;
            webView.loadUrl(HOME_URL);
        });

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER);
        content.setPadding(dp(24), dp(24), dp(24), dp(24));
        content.setBackgroundColor(Color.rgb(16, 27, 53));
        content.addView(message, matchWrap());
        LinearLayout.LayoutParams retryParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        retryParams.topMargin = dp(12);
        content.addView(retry, retryParams);

        errorView = content;
        webContainer.addView(errorView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        if (progressBar != null) progressBar.setVisibility(View.GONE);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (webView != null) webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
