/* Afterglow local selector: the Android app stores the selected payload.
   The PS4 browser reads that choice from the same phone-hosted server. */
var countEl = document.getElementById("count");
var DONE = false;
try { DONE = new URLSearchParams(location.search).get("afterglow") === "done"; } catch (e) {}
if (DONE) {
  countEl.textContent = "kernel already patched";
} else {
  countEl.textContent = "Loading selected HEN...";
  fetch("./config", { cache: "no-store" })
    .then(function (r) { if (!r.ok) throw new Error("config"); return r.json(); })
    .then(function (cfg) {
      var mode = cfg.mode === "hen" ? "hen" : "goldhen";
      countEl.textContent = "Starting " + (mode === "hen" ? "PS4-HEN" : "GoldHEN") + "...";
      setTimeout(function () {
        location.href = "jb.html?log=1&afterglow=run&mode=" + mode;
      }, 1200);
    })
    .catch(function () {
      countEl.textContent = "Configuration unavailable - using GoldHEN";
      setTimeout(function () {
        location.href = "jb.html?log=1&afterglow=run&mode=goldhen";
      }, 1500);
    });
}