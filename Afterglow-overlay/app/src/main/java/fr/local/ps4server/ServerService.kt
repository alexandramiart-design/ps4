package fr.local.ps4server
import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.net.URLConnection
class ServerService : Service() {
 companion object { const val PORT=8000; const val ACTION_REFRESH="fr.local.ps4server.REFRESH"; const val ACTION_STOP="fr.local.ps4server.STOP" }
 private var server:NanoHTTPD?=null
 private lateinit var siteRoot:File
 override fun onCreate(){ super.onCreate(); siteRoot=File(filesDir,"site") }
 override fun onStartCommand(intent:Intent?,flags:Int,startId:Int):Int {
  if(intent?.action==ACTION_STOP){ stopSelf(); return START_NOT_STICKY }
  val channel="ps4_local_server"
  if(Build.VERSION.SDK_INT>=26) getSystemService(NotificationManager::class.java).createNotificationChannel(NotificationChannel(channel,"Afterglow",NotificationManager.IMPORTANCE_LOW))
  val notification=NotificationCompat.Builder(this,channel).setContentTitle("Afterglow").setContentText("Serveur HTTP actif sur le port $PORT").setSmallIcon(R.mipmap.ic_launcher).setOngoing(true).build()
  startForeground(1,notification)
  try { if(intent?.action==ACTION_REFRESH){server?.stop();server=null;installAssets(true)} else installAssets(false); if(server==null) startHttpServer() } catch(_:Exception){stopSelf()}
  return START_STICKY
 }
 private fun installAssets(overwrite:Boolean){
  if(!siteRoot.exists()) siteRoot.mkdirs()
  fun copyDir(assetPath:String,dest:File){
   val children=assets.list(assetPath)?: emptyArray()
   if(children.isEmpty()){ if(overwrite||!dest.exists()) assets.open(assetPath).use{input->dest.parentFile?.mkdirs();dest.outputStream().use{output->input.copyTo(output)}};return }
   dest.mkdirs(); for(child in children) copyDir("$assetPath/$child",File(dest,child))
  }
  copyDir("site",siteRoot)
 }
 private fun startHttpServer(){
  server=object:NanoHTTPD(PORT){
   override fun serve(session:IHTTPSession):Response {
    val requested=session.uri.trimStart('/').ifBlank{"index.html"}; val target=File(siteRoot,requested).canonicalFile; val base=siteRoot.canonicalFile
    if(target.path!=base.path&&!target.path.startsWith(base.path+File.separator)) return newFixedLengthResponse(Response.Status.FORBIDDEN,"text/plain","Forbidden")
    if(!target.isFile) return newFixedLengthResponse(Response.Status.NOT_FOUND,"text/plain","Not found")
    val mime=when(target.extension.lowercase()){
     "html","htm"->"text/html; charset=utf-8";"js","mjs"->"text/javascript; charset=utf-8";"css"->"text/css; charset=utf-8";"json","map"->"application/json";"svg"->"image/svg+xml";"png"->"image/png";"jpg","jpeg"->"image/jpeg";"gif"->"image/gif";"ico"->"image/x-icon";"bin","iso"->"application/octet-stream";else->URLConnection.guessContentTypeFromName(target.name)?:"application/octet-stream"
    }
    val response=newChunkedResponse(Response.Status.OK,mime,target.inputStream()); val isImage=target.extension.lowercase() in setOf("png","jpg","jpeg","gif","webp","ico")
    response.addHeader("Cache-Control",if(isImage)"public, max-age=604800" else "no-store, must-revalidate")
    if(!isImage){response.addHeader("Pragma","no-cache");response.addHeader("Expires","0")}
    response.addHeader("X-Content-Type-Options","nosniff"); return response
   }
  }.also{it.start(NanoHTTPD.SOCKET_READ_TIMEOUT,false)}
 }
 override fun onDestroy(){server?.stop();server=null;super.onDestroy()}
 override fun onBind(intent:Intent?):IBinder?=null
}