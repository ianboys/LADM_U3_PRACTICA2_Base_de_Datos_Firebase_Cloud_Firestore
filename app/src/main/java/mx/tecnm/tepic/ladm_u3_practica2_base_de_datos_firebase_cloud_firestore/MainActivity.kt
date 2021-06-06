package mx.tecnm.tepic.ladm_u3_practica2_base_de_datos_firebase_cloud_firestore

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    var baseRemota = FirebaseFirestore.getInstance()
    var ordenes = ArrayList<String>()
    var listaID = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cargarOrdenes()

        btnAgregarProductos.setOnClickListener {
            insertarOrden()
        }
    }

    private fun insertarOrden() {
        var id = ""
        var formato = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        var fecha = formato.format(Date())
        //var total = calcularTotal()
        var entregado = false
        var datosInsertar = hashMapOf(
            "nombre" to txtNombreCliente.text.toString(),
            "celular" to txtTelefono.text.toString(),
            "entregado" to entregado,
            //"total" to total,
            "fecha" to fecha
        )
        baseRemota.collection("Pedidos")
            .add(datosInsertar)
            .addOnSuccessListener {
                alerta("EXITO! SE INSERTO CORRECTAMENTE")
                limpiarCampos()
                id = it.id.toString()

                //Cambiar de activity
                var intent = Intent(this,MainActivity2::class.java)
                intent.putExtra("idElegido",id)
                startActivity(intent)
            }
            .addOnFailureListener {
                mensaje("ERROR! no se pudo insertar")
            }
    }

    public fun cargarOrdenes() {
        baseRemota.collection("Pedidos")
            .addSnapshotListener { querySnapshot, error ->
                if (error != null){
                    mensaje(error.message!!)
                    return@addSnapshotListener
                }
                ordenes.clear()
                listaID.clear()
                for (document in querySnapshot!!){
                    var cadena = "${document.getString("nombre")} - ${document.get("Total")} - ${document.get("entregado")}"
                    ordenes.add(cadena)
                    listaID.add(document.id.toString())
                }
                listaOrdenes.adapter = ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,ordenes)
                listaOrdenes.setOnItemClickListener { parent, view, position, id ->
                    dialogoActualiza(position)
                }
            }
    }

    private fun dialogoActualiza(position: Int) {
        var idElegido = listaID.get(position)
        AlertDialog.Builder(this).setTitle("ATENCION!!")
            .setMessage("LA ORDEN No. ${ordenes.get(position)}\n HA SIDO ENTREGADA?")
            .setPositiveButton("SI"){d, i->
                actualizarOrden(idElegido,true)
            }
            .setNeutralButton("NO"){d,i->
                actualizarOrden(idElegido,false)
            }
            .setNegativeButton("CANCELAR"){d,i->}
            .show()
    }

    private fun actualizarOrden(idElegido: String, bandera: Boolean) {
        if(bandera){
            baseRemota.collection("Pedidos")
                .document(idElegido)
                .update("entregado",true)
                .addOnSuccessListener {
                    mensaje("ESTADO DE LA ORDEN ACTUALIZADA")
                    cargarOrdenes()
                }
        }else{
            baseRemota.collection("Pedidos")
                .document(idElegido)
                .update("entregado",false)
                .addOnSuccessListener {
                    mensaje("ESTADO DE LA ORDEN ACTUALIZADA")
                    cargarOrdenes()
                }
        }
    }

    private fun limpiarCampos(){
        txtNombreCliente.setText("")
        txtTelefono.setText("")
    }

    private fun mensaje(s: String) {
        AlertDialog.Builder(this).setTitle("ATENCION")
            .setMessage(s)
            .setPositiveButton("OK"){ d,i-> }
            .show()
    }

    private fun alerta(s: String) {
        Toast.makeText(this,s, Toast.LENGTH_LONG).show()
    }
}