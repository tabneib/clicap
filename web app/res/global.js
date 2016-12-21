var standardgroesse = 11;
var mingroesse = 10;
var maxgroesse = 14;
var aktgroesse = standardgroesse;
function oeffneFenster(adresse,fenster,breite,hoehe,optionen){
var scrollbars=false;
if(breite>screen.availWidth){
breite=screen.availWidth;
scrollbars=true;
}
if(hoehe>screen.availHeight){
hoehe=screen.availHeight;
scrollbars=true;
}
if(scrollbars) optionen+=",scrollbars=yes";
else optionen+=",scrollbars=no";
fenster=window.open(adresse,fenster,"width="+breite+",height="+hoehe+","+optionen);
fenster.focus();
}
function fontsize(action){
switch(action){
case 'inkrement':
if(aktgroesse+1<=maxgroesse) aktgroesse++;
break;
case 'dekrement':
if(aktgroesse-1>=mingroesse) aktgroesse--;
break;
default:
aktgroesse=standardgroesse;
}
$('body').css("fontSize",aktgroesse+'px');
$.cookie('fontsize',aktgroesse,{expires:365,path:'/',domain:'.tu-darmstadt.de'});
}
$(document).ready(function(){
$("#topnavigation > form > a").show();
$("#topnavigation input[type='submit']").hide();
if($.cookie("fontsize")){
aktgroesse=parseInt($.cookie("fontsize"));
if(aktgroesse!=standardgroesse){
$("body").css("fontSize",aktgroesse+"px");
}
}
$("li.hauptmenuepunkt").hover(function(){
$("ul.klappbox").not($(this).children("ul.klappbox")).slideUp("fast");
$(this).stopTime("slideUp");
$(this).oneTime(250,"slideDown",function() {
$(this).children("ul.klappbox").slideDown("fast");
});
},function(){
$(this).stopTime("slideDown");
$(this).oneTime(500,"slideUp",function() {
$(this).children("ul.klappbox").slideUp("fast");
});
}
);
$("li.hauptmenuepunkt > a").focus(function(){
$("ul.klappbox").hide();
$(this).next("ul.klappbox").show();
}
);
$("#logo > #bildschirm > a").focus(function(){
$("ul.klappbox").hide();
}
);
$("#query_top").removeAttr("title");
if($("#query_top").val()=="" || $("#query_top").val()==suchetext){
$("#query_top").addClass("grau");
$("#query_top").val(suchetext);
}
$("#query_top").click(function(){
if($(this).val()==suchetext){
$(this).val("");
$(this).removeClass("grau");
}
});
$("#query_top").focus(function(){
if($(this).val()==suchetext){
$(this).val("");
$(this).removeClass("grau");
}
});
$("#query_top").blur(function(){
if($(this).val()==""){
$(this).addClass("grau");
$(this).val(suchetext);
}
});
$("#query_abschicken").click(function(){
if($("#query_top").val()==suchetext){
$("#query_top").val("");
$("#query_top").removeClass("grau");
}
});
$('a.email').each(function(){
var email = $(this).attr("rel").replace('%2F','@').replace('%3A',':');
$(this).removeAttr("rel");
var href = email;
if(href.substring(0,7) != "mailto:") href = "mailto:" + href;
$(this).attr("href",href);
$(this).attr("title",email);
});
$('span.email').each(function(){
var emailtext=$(this).text();
var email=emailtext.replace("tu-…","tu-darmstadt.de").replace("tu-...","tu-darmstadt.de");
$(this).replaceWith('<a class="icon email" href="mailto:'+email+'" title="E-Mail an: '+email+'">'+emailtext+'</a>');
});
});