package com.macoshapps.wintracker.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

private data class ContestEntry(val id:String,val title:String,val start:String,val end:String,val prize:String,val type:String,val status:String,val notes:String,val image:String)
private fun loadContests(c:Context):List<ContestEntry> = runCatching {
 val a=JSONArray(c.getSharedPreferences("contests",0).getString("items","[]"))
 (0 until a.length()).map { val x=a.getJSONObject(it); ContestEntry(x.optString("id"),x.optString("title"),x.optString("start"),x.optString("end"),x.optString("prize"),x.optString("type"),x.optString("status"),x.optString("notes"),x.optString("image")) }
}.getOrDefault(emptyList())
private fun saveContests(c:Context,list:List<ContestEntry>) {
 val a=JSONArray(); list.forEach { x-> a.put(JSONObject().put("id",x.id).put("title",x.title).put("start",x.start).put("end",x.end).put("prize",x.prize).put("type",x.type).put("status",x.status).put("notes",x.notes).put("image",x.image)) }
 c.getSharedPreferences("contests",0).edit().putString("items",a.toString()).apply()
}
@Composable
fun ContestsScreen(lang:String) {
 val c=LocalContext.current
 var contests by remember { mutableStateOf(loadContests(c)) }
 var filter by remember { mutableStateOf("") }
 var editor by remember { mutableStateOf<ContestEntry?>(null) }
 var open by remember { mutableStateOf(false) }
 val t:(String,String,String)->String={pl,en,es->when(lang.uppercase()){"EN"->en;"ES"->es;else->pl}}
 Column(Modifier.fillMaxSize().padding(16.dp)) {
  Text(t("Konkursy","Contests","Concursos"),style=MaterialTheme.typography.headlineMedium)
  Spacer(Modifier.height(12.dp))
  Button(onClick={editor=null;open=true},modifier=Modifier.fillMaxWidth()){Text(t("+ Dodaj konkurs","+ Add contest","+ Añadir concurso"))}
  OutlinedTextField(filter,{filter=it},label={Text(t("Szukaj po nazwie lub nagrodzie","Search title or prize","Buscar título o premio"))},modifier=Modifier.fillMaxWidth())
  Spacer(Modifier.height(10.dp))
  LazyVerticalGrid(columns=GridCells.Fixed(2),horizontalArrangement=Arrangement.spacedBy(10.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
   items(contests.filter{filter.isBlank()||it.title.contains(filter,true)||it.prize.contains(filter,true)||it.type.contains(filter,true)}) { x->
    Card(onClick={editor=x;open=true},shape=RoundedCornerShape(14.dp)) {
     Column(Modifier.padding(10.dp)) {
      if(x.image.isNotEmpty()) {
       val bmp=remember(x.image){runCatching{c.contentResolver.openInputStream(Uri.parse(x.image))?.use{BitmapFactory.decodeStream(it)}}.getOrNull()}
       if(bmp!=null) Image(bmp.asImageBitmap(),null,Modifier.fillMaxWidth().height(110.dp),contentScale=ContentScale.Crop)
      }
      Text(x.title,style=MaterialTheme.typography.titleMedium)
      Text(x.type,style=MaterialTheme.typography.labelSmall)
      Text(x.end,style=MaterialTheme.typography.bodySmall)
      Text(x.prize,style=MaterialTheme.typography.bodySmall)
      Text(x.status,style=MaterialTheme.typography.labelSmall)
     }
    }
   }
  }
 }
 if(open) {
  var title by remember(editor){mutableStateOf(editor?.title?:"")}
  var start by remember(editor){mutableStateOf(editor?.start?:"")}
  var end by remember(editor){mutableStateOf(editor?.end?:"")}
  var prize by remember(editor){mutableStateOf(editor?.prize?:"")}
  var type by remember(editor){mutableStateOf(editor?.type?:"Darmowy")}
  var status by remember(editor){mutableStateOf(editor?.status?:"Zapisany")}
  var notes by remember(editor){mutableStateOf(editor?.notes?:"")}
  var image by remember(editor){mutableStateOf(editor?.image?:"")}
  val picker=rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()){uri->if(uri!=null){runCatching{c.contentResolver.takePersistableUriPermission(uri,android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)};image=uri.toString()}}
  AlertDialog(onDismissRequest={open=false},title={Text(t("Konkurs","Contest","Concurso"))},text={
   Column(Modifier.heightIn(max=490.dp)) {
    OutlinedTextField(title,{title=it},label={Text(t("Nazwa","Title","Título"))})
    OutlinedTextField(start,{start=it},label={Text(t("Od (RRRR-MM-DD)","Start (YYYY-MM-DD)","Inicio"))})
    OutlinedTextField(end,{end=it},label={Text(t("Do (RRRR-MM-DD)","End (YYYY-MM-DD)","Fin"))})
    OutlinedTextField(prize,{prize=it},label={Text(t("Nagrody / tagi","Prizes / tags","Premios / etiquetas"))})
    OutlinedTextField(type,{type=it},label={Text(t("Rodzaj (darmowy / zakup)","Type (free / purchase)","Tipo"))})
    OutlinedTextField(status,{status=it},label={Text(t("Status udziału","Participation status","Estado"))})
    OutlinedTextField(notes,{notes=it},label={Text(t("Notatki","Notes","Notas"))})
    TextButton(onClick={picker.launch(arrayOf("image/*"))}){Text(t("Wybierz plakat","Choose poster","Elegir cartel"))}
   }
  },confirmButton={TextButton(enabled=title.isNotBlank(),onClick={
   val x=ContestEntry(editor?.id?:UUID.randomUUID().toString(),title,start,end,prize,type,status,notes,image)
   contests=if(editor==null)listOf(x)+contests else contests.map{if(it.id==x.id)x else it}
   saveContests(c,contests);open=false
  }){Text(t("Zapisz","Save","Guardar"))}},dismissButton={
   Row {
    if(editor!=null) TextButton(onClick={contests=contests.filter{it.id!=editor!!.id};saveContests(c,contests);open=false}){Text(t("Usuń","Delete","Eliminar"))}
    TextButton(onClick={open=false}){Text(t("Anuluj","Cancel","Cancelar"))}
   }
  })
 }
}
