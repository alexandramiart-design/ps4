/* Fixed GoldHEN launcher. No PS4N/PS4-HEN selector and no Android config endpoint. */
var AUTOSTART_SECONDS=5, STORE_KEY="afterglow.autostart";
var form=document.getElementById("pick"), autoBox=document.getElementById("auto");
var countEl=document.getElementById("count"), cacheEl=document.getElementById("cache");
var ticker=null, gone=false;
function stopClock(){if(ticker!==null){clearInterval(ticker);ticker=null;}if(countEl)countEl.textContent="";}
function go(){if(gone)return;gone=true;stopClock();location.href="jb.html?log=1&afterglow=run&mode=goldhen";}
function start(){stopClock();go();}
function startClock(){var left=AUTOSTART_SECONDS;countEl.textContent="starting in "+left+"s";ticker=setInterval(function(){left--;if(left<=0){start();return;}countEl.textContent="starting in "+left+"s";},1000);}
try{autoBox.checked=localStorage.getItem(STORE_KEY)!=="0";}catch(e){}
form.addEventListener("submit",function(e){e.preventDefault();start();});
form.addEventListener("change",function(e){if(e.target===autoBox){try{localStorage.setItem(STORE_KEY,autoBox.checked?"1":"0");}catch(x){}if(autoBox.checked)startClock();else stopClock();}});
try{var c=window.applicationCache;if(c){c.addEventListener("checking",function(){cacheEl.textContent="checking for updates";});c.addEventListener("downloading",function(){cacheEl.textContent="loading files - hold on";});c.addEventListener("cached",function(){cacheEl.textContent="";});c.addEventListener("noupdate",function(){cacheEl.textContent="";});}}catch(e){}
if(autoBox.checked)startClock();
