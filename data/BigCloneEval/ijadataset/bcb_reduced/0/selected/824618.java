package ve.usb.buscame;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.io.InputStream;
import java.util.ArrayList;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AgregarGrupos extends Activity {

    TextView txt;

    String grupo;

    String result = "";

    public static final String auth = "http://" + Login.ipServidor + "/agregarGrupo.php";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.agregar_grupos);
        LinearLayout rootLayout = new LinearLayout(getApplicationContext());
        txt = new TextView(getApplicationContext());
        rootLayout.addView(txt);
    }

    private String getServerData(String returnString) {
        InputStream is = null;
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        nameValuePairs.add(new BasicNameValuePair("creador", Login.usuario));
        nameValuePairs.add(new BasicNameValuePair("grupo", grupo));
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(auth);
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
        } catch (Exception e) {
            Log.e("log_tag", "Error in http connection " + e.toString());
        }
        return returnString;
    }

    protected void onResume() {
        super.onResume();
        final Button boton = (Button) findViewById(R.id.btnEnviar);
        boton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                txt.setText("Connecting...");
                EditText et = (EditText) findViewById(R.id.textoUsuario);
                grupo = et.getText().toString();
                et.setText(grupo);
                if (grupo.equals("")) {
                    Context context = getApplicationContext();
                    CharSequence text = "No puede crear un grupo sin nombre.";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                    et.setText("");
                } else {
                    txt.setText(getServerData(auth));
                    Intent intent = new Intent();
                    setResult(RESULT_OK, intent);
                    finish();
                    Context context = getApplicationContext();
                    CharSequence text = "Nuevo grupo creado: " + grupo;
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                    et.setText("");
                }
            }
        });
        final Button btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                EditText et = (EditText) findViewById(R.id.textoUsuario);
                et.setText("");
            }
        });
    }
}
