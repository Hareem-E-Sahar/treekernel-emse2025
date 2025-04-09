public class Test {        @Override
        String getJavaScript(int appletID, JmolInstance instance) {
            String jsString = "<table id=\"AnimContrl\" class=\"AnimContrlCSS\">";
            jsString += "<tbody><tr><td>" + GT.escapeHTML(GT._("Animation")) + "</td></tr><tr><td><table><tbody>";
            jsString += "<tr><td><button title=\"" + GT.escapeHTML(GT._("First Frame")) + "\" onclick=\"void(jmolScriptWait(\'frame rewind\'," + appletID + "));\">";
            jsString += "<img src = \"firstButton.png\"></button></td>";
            jsString += "<td><button title=\"" + GT.escapeHTML(GT._("Previous Frame")) + "\" onclick=\"void(jmolScriptWait(\'frame previous\'," + appletID + "));\">";
            jsString += "<img src = \"prevButton.png\" ></button></td>";
            jsString += "<td><button title=\"" + GT.escapeHTML(GT._("Play")) + "\" onclick=\"void(jmolScriptWait(\'frame play\'," + appletID + "));\">";
            jsString += "<img src = \"playButton.png\"></button></td>";
            jsString += "<td><button title=\"" + GT.escapeHTML(GT._("Next Frame")) + "\" onclick=\"void(jmolScriptWait(\'frame next\'," + appletID + "));\">";
            jsString += "<img src = \"nextButton.png\"></button></td>";
            jsString += "<td><button title=\"" + GT.escapeHTML(GT._("Pause")) + "\" onclick=\"void(jmolScriptWait(\'frame pause\'," + appletID + "));\">";
            jsString += "<img src = \"pauseButton.png\"></button></td>";
            jsString += "<td><button title=\"" + GT.escapeHTML(GT._("Last Frame")) + "\" onclick=\"void(jmolScriptWait(\'frame last\'," + appletID + "));\">";
            jsString += "<img src = \"lastButton.png\"></button></td>";
            jsString += "</tr></tbody></table><table><tbody><tr><td>" + GT.escapeHTML(GT._("Mode:")) + "</td>";
            jsString += "<td id=\"jmol_loop_" + appletID + "\"><button title=\"" + GT.escapeHTML(GT._("Loop")) + "\" onclick=\"jmol_animationmode(\'loop\'," + appletID + ");\">";
            jsString += "<img src = \"playLoopButton.png\" ></button></td>";
            jsString += "<td id=\"jmol_palindrome_" + appletID + "\"><button title=\"" + GT.escapeHTML(GT._("Palindrome")) + "\" onclick=\"jmol_animationmode(\'palindrome\', " + appletID + ");\">";
            jsString += "<img src = \"playPalindromeButton.png\" ></button></td>";
            jsString += "<td id=\"jmol_playOnce_" + appletID + "\" style=\"background:blue;\"><button title=\"" + GT.escapeHTML(GT._("Play Once")) + "\" style=\"font-size:0px\" onclick=\"jmol_animationmode(\'playOnce\', " + appletID + ");\">";
            jsString += "<img src = \"playOnceButton.png\" ></button></td></tr></tbody></table></td></tr></tbody></table>";
            return (jsString);
        }
}