/* The payload is selected in the Android app and read from its local config endpoint. */
var AUTOSTART_SECONDS=5, STORE_KEY="afterglow.autostart";
var form=document.getElementById("pick"), autoBox=document.getElementById("auto");
var countEl=document.getElementById("count"), cacheEl=document.getElementById("cache");
var selectedEl=document.getElementById("selected"), ticker=null, gone=false, selectedPayload="goldhen";
function loadConfig(){
 return fetch("/config",{cache:"no-store"}).then(function(r){if(!r.ok)throw Error("config");return r.json();})
 .then(function(c){selectedPayload=c&&c.mode==="hen"?"hen":"goldhen"; if(selectedEl)selectedEl.textContent=selectedPayload==="hen"?"Payload choisi : PS4N / PS4-HEN":"Payload choisi : GoldHEN";})
 .catch(function(){selectedPayload="goldhen";if(selectedEl)selectedEl.textContent="Configuration indisponible — GoldHEN par défaut";});
}
function stopClock(){if(ticker!==null){clearInterval(ticker);ticker=null;}if(countEl)countEl.textContent="";}
function go(){if(gone)return;gone=true;stopClock();location.href=selectedPayload==="hen"?"jb.html?log=1&afterglow=run&payload=hen.bin":"jb.html?log=1&afterglow=run&mode=goldhen";}
function start(){stopClock();loadConfig().then(go);}
function startClock(){var left=AUTOSTART_SECONDS;countEl.textContent="starting in "+left+"s";ticker=setInterval(function(){left--;if(left<=0){start();return;}countEl.textContent="starting in "+left+"s";},1000);}
try{autoBox.checked=localStorage.getItem(STORE_KEY)!=="0";}catch(e){}
form.addEventListener("submit",function(e){e.preventDefault();start();});
form.addEventListener("change",function(e){if(e.target===autoBox){try{localStorage.setItem(STORE_KEY,autoBox.checked?"1":"0");}catch(x){}if(autoBox.checked)startClock();else stopClock();}});
try{var c=window.applicationCache;if(c){c.addEventListener("checking",function(){cacheEl.textContent="checking for updates";});c.addEventListener("downloading",function(){cacheEl.textContent="loading files - hold on";});c.addEventListener("cached",function(){cacheEl.textContent="";});c.addEventListener("noupdate",function(){cacheEl.textContent="";});}}catch(e){}
loadConfig().then(function(){if(autoBox.checked)startClock();});
