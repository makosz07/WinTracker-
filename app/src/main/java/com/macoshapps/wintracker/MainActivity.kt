package com.macoshapps.wintracker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

data class Win(val title:String,val date:String,val value:String,val tags:String)
class MainActivity:ComponentActivity(){
 private val prefs by lazy {getSharedPreferences("wins",MODE_PRIVATE)}
 private val entries= mutableStateListOf<Win>()
 private var language by mutableStateOf("PL")
 private fun save(){ val a=JSONArray();entries.forEach { a.put(JSONObject().put("title",it.title).put("date",it.date).put("value",it.value).put("tags",it.tags)) };prefs.edit().putString("wins",a.toString()).apply() }
 private fun load(json:String){val a=JSONArray(json);entries.clear();for(i in 0 until a.length()){val o=a.getJSONObject(i);entries.add(Win(o.optString("title"),o.optString("date"),o.optString("value"),o.optString("tags")))};save()}
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState)
  language=prefs.getString("lang","PL")?:"PL"
  runCatching{load(prefs.getString("wins","[]")?:"[]")}
  setContent { MaterialTheme { Screen() } }
 }
 override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){super.onActivityResult(requestCode,resultCode,data);if(resultCode!=Activity.RESULT_OK)return
  val uri=data?.data?:return
  if(requestCode==1){contentResolver.openOutputStream(uri)?.use{it.write((prefs.getString("wins","[]")?:"[]").toByteArray())}}
  if(requestCode==2){runCatching{contentResolver.openInputStream(uri)?.bufferedReader()?.use{load(it.readText())}}}
 }
 private fun export(){startActivityForResult(Intent(Intent.ACTION_CREATE_DOCUMENT).setType("application/json").addCategory(Intent.CATEGORY_OPENABLE).putExtra(Intent.EXTRA_TITLE,"wintracker-backup.json"),1)}
 private fun import(){startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).setType("application/json").addCategory(Intent.CATEGORY_OPENABLE),2)}
 @Composable private fun Screen(){
 var page by remember { mutableStateOf(0) };var title by remember {mutableStateOf("")};var date by remember {mutableStateOf(LocalDate.now().toString())};var value by remember {mutableStateOf("")};var tags by remember {mutableStateOf("")}
 val t=when(language){"EN"->listOf("Wins","Statistics","Settings","Add win","Title","Date (YYYY-MM-DD)","Value","Tags","Save","Export JSON","Import JSON","Language","About","Total wins","Total value");"ES"->listOf("Premios","Estadísticas","Ajustes","Añadir premio","Nombre","Fecha (AAAA-MM-DD)","Valor","Etiquetas","Guardar","Exportar JSON","Importar JSON","Idioma","Acerca de","Premios totales","Valor total");else->listOf("Wygrane","Statystyki","Ustawienia","Dodaj wygraną","Nazwa","Data (RRRR-MM-DD)","Wartość","Tagi","Zapisz","Eksport JSON","Import JSON","Język","O aplikacji","Liczba wygranych","Łączna wartość")}
 Scaffold(topBar={Surface(tonalElevation=3.dp){Text("WinTracker  •  Macosh Apps",Modifier.fillMaxWidth().padding(20.dp),style=MaterialTheme.typography.titleLarge)}},bottomBar={NavigationBar{listOf(t[0],t[1],t[2]).forEachIndexed{i,s->NavigationBarItem(selected=page==i,onClick={page=i},icon={Text(listOf("★","▥","⚙")[i])},label={Text(s)})}}}){pad->
 Column(Modifier.padding(pad).padding(16.dp)){
 when(page){
 0->{Text(t[3],style=MaterialTheme.typography.titleLarge);Spacer(Modifier.height(12.dp))
 listOf(t[4],t[5],t[6],t[7]).forEachIndexed{i,label->
 val v=listOf(title,date,value,tags)[i]
 OutlinedTextField(value=v,onValueChange={when(i){0->title=it;1->date=it;2->value=it;else->tags=it}},label={Text(label)},modifier=Modifier.fillMaxWidth())}
 Button(onClick={if(title.isNotBlank()){entries.add(0,Win(title,date,value,tags));save();title="";value="";tags=""}},modifier=Modifier.fillMaxWidth()){Text(t[8])}
 LazyColumn{items(entries){w->Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){Column(Modifier.padding(12.dp)){Text(w.title,style=MaterialTheme.typography.titleMedium);Text("${w.date}  •  ${w.value}");Text(w.tags);TextButton(onClick={entries.remove(w);save()}){Text("✕")}}}}}
 }
 1->{Text("${t[13]}: ${entries.size}",style=MaterialTheme.typography.headlineSmall);Text("${t[14]}: ${entries.sumOf{it.value.replace(",",".").toDoubleOrNull()?:0.0}}")
 entries.groupingBy{it.date.take(4)}.eachCount().toSortedMap().forEach{(year,count)->Text("$year: $count")}
 Text("Tagi / Tags:");entries.flatMap{it.tags.split(",").map{v->v.trim()}.filter{v->v.isNotEmpty()}}.groupingBy{it}.eachCount().forEach{(tag,count)->Text("$tag: $count")}
 }
 else->{Text(t[11],style=MaterialTheme.typography.titleLarge);listOf("PL","EN","ES").forEach{lang->Row{RadioButton(selected=language==lang,onClick={language=lang;prefs.edit().putString("lang",lang).apply()});Text(lang,Modifier.padding(12.dp))}}
 Button(onClick={export()}){Text(t[9])};Button(onClick={import()}){Text(t[10])};HorizontalDivider();Text("${t[12]}: WinTracker v0.1.0 • Macosh Apps")}
 }
 }
 }
 }
}
