package mx.tecnm.tepic.ladm_u3_practica2_base_de_datos_firebase_cloud_firestore

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main2.*

class MainActivity2 : AppCompatActivity() {
    var baseRemota = FirebaseFirestore.getInstance()
    var listaPedidos = ArrayList<Pedido>()
    var pedidos = ArrayList<String>()
    var idOrden = ""
    var contador = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        var extra = intent.extras

        idOrden = extra!!.getString("idElegido")!!

        btnInsertarProducto.setOnClickListener {
            insertarProducto()
        }

        btnConfirmar.setOnClickListener {
            insertarOrden()
        }
    }

    private fun insertarProducto() {
        var pedido = Pedido(txtDescripcion.text.toString(),
            txtCantidad.text.toString().toInt(),
            txtPrecio.text.toString().toInt())
        listaPedidos.add(pedido)
        limpiarCampos()
        cargarPedido()
        alerta("Se ingreso el pedido a la orden")
    }


    private fun insertarOrden() {
        listaPedidos.forEach {
            var datosPedido = hashMapOf(
                "Total" to sumaTotal(),
                "Pedido" to hashMapOf(
                    "Item${contador.toString()}" to hashMapOf(
                        "Descripcion" to it.descr,
                        "Cantidad" to it.cant,
                        "Precio" to it.tot
                    )
                )
            )
            baseRemota.collection("Pedidos")
                .document(idOrden)
                .set(datosPedido, SetOptions.merge())
                .addOnSuccessListener {
                    alerta("EXITO! SE INSERTO CORRECTAMENTE")
                }
                .addOnFailureListener {
                    mensaje("ERROR: ${it.message!!}")
                }
            contador++
        }
        contador=0
        //MainActivity().cargarOrdenes()
        finish()
    }

    private fun sumaTotal():Int {
        var total = 0
        listaPedidos.forEach {
            total+=it.tot
        }
        return total
    }

    private fun cargarPedido() {
        pedidos.clear()
        listaPedidos.forEach {
            var cadena = "${it.descr} - ${it.cant} - ${it.tot}"
            pedidos.add(cadena)
        }
        listaProductos.adapter = ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,pedidos)
        listaProductos.setOnItemClickListener { parent, view, position, id ->
            dialogoElimina(position)
        }

    }

    private fun dialogoElimina(position: Int) {
        AlertDialog.Builder(this).setTitle("ATENCION!!")
            .setMessage("DESEA ELIMINAR EL PEDIDO \n ${position}?")
            .setPositiveButton("SI"){d, i->
                listaPedidos.removeAt(position)
                cargarPedido()
                alerta("PEDIDO ELIMINADO EXITOSAMENTE")
            }
            .setNegativeButton("NO"){d,i->}
            .show()
    }

    private fun limpiarCampos(){
        txtDescripcion.setText("")
        txtCantidad.setText("")
        txtPrecio.setText("")
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