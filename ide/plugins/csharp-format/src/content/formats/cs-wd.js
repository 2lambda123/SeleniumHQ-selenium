/*
 * Formatter for Selenium 2 / WebDriver .NET (C#) client.
 */

var subScriptLoader = Components.classes["@mozilla.org/moz/jssubscript-loader;1"].getService(Components.interfaces.mozIJSSubScriptLoader);
subScriptLoader.loadSubScript('chrome://selenium-ide/content/formats/webdriver.js', this);

function testClassName(testName) {
  return testName.split(/[^0-9A-Za-z]+/).map(
      function(x) {
        return capitalize(x);
      }).join('');
}

function testMethodName(testName) {
  return "The" + capitalize(testName) + "Test";
}

function nonBreakingSpace() {
  return "\"\\u00a0\"";
}

function array(value) {
  var str = 'new String[] {';
  for (var i = 0; i < value.length; i++) {
    str += string(value[i]);
    if (i < value.length - 1) str += ", ";
  }
  str += '}';
  return str;
}

Equals.prototype.toString = function() {
  return this.e1.toString() + " == " + this.e2.toString();
};

Equals.prototype.assert = function() {
  return "Assert.AreEqual(" + this.e1.toString() + ", " + this.e2.toString() + ");";
};

Equals.prototype.verify = function() {
  return verify(this.assert());
};

NotEquals.prototype.toString = function() {
  return this.e1.toString() + " != " + this.e2.toString();
};

NotEquals.prototype.assert = function() {
  return "Assert.AreNotEqual(" + this.e1.toString() + ", " + this.e2.toString() + ");";
};

NotEquals.prototype.verify = function() {
  return verify(this.assert());
};

function joinExpression(expression) {
  return "String.Join(\",\", " + expression.toString() + ")";
}

function statement(expression) {
  return expression.toString() + ';';
}

function assignToVariable(type, variable, expression) {
  return capitalize(type) + " " + variable + " = " + expression.toString();
}

function ifCondition(expression, callback) {
  return "if (" + expression.toString() + ")\n{\n" + callback() + "}";
}

function assertTrue(expression) {
  return "Assert.IsTrue(" + expression.toString() + ");";
}

function assertFalse(expression) {
  return "Assert.IsFalse(" + expression.toString() + ");";
}

function verify(statement) {
  return "try\n" +
      "{\n" +
      indents(1) + statement + "\n" +
      "}\n" +
      "catch (AssertionException e)\n" +
      "{\n" +
      indents(1) + "verificationErrors.Append(e.Message);\n" +
      "}";
}

function verifyTrue(expression) {
  return verify(assertTrue(expression));
}

function verifyFalse(expression) {
  return verify(assertFalse(expression));
}

RegexpMatch.patternToString = function(pattern) {
  if (pattern != null) {
    //value = value.replace(/^\s+/, '');
    //value = value.replace(/\s+$/, '');
    pattern = pattern.replace(/\\/g, '\\\\');
    
    //pattern = pattern.replace(/\"/g, '\\"');
    //f'ing javascript doesn't support lookbehind regex otherwise I could just use /(?<! \+ )"(?! \+ )/g instead of the following 4 commands
    //the following 4 lines provide a way for variables to be used by looking for ' + ' with quotes
    //eg. verifyTextPresent || ${date}       date gets transoformed to " + date + "
    //     this will make sure those extra quotes don't get commented out with "\"
    pattern = pattern.replace(/ \+ \"/g, ' + \"\\'); 
    pattern = pattern.replace(/\" \+ /g, '\"\\ + '); //so I'm just adding a '\' after a quote
    pattern = pattern.replace(/\"(?!\\)/g, '\\"'); //replace every quote unless following by "\"
    pattern = pattern.replace(/\"\\/g, '\"'); //remove that "\" I put in
    
    pattern = pattern.replace(/\r/g, '\\r');
    pattern = pattern.replace(/\n/g, '(\\n|\\r\\n)');
    return '"' + pattern + '"';
  } else {
    return '""';
  }
};

RegexpMatch.prototype.toString = function() {
  return "Regex.IsMatch(" + this.expression + ", " + RegexpMatch.patternToString(this.pattern) + ")";
};

function waitFor(expression) {
  return "for (int second = 0;; second++) {\n" +
      indents(1) + 'if (second >= 60) Assert.Fail("timeout");\n' +
      indents(1) + "try\n" +
      indents(1) + "{\n" +
      (expression.setup ? indents(2) + expression.setup() + "\n" : "") +
      indents(2) + "if (" + expression.toString() + ") break;\n" +
      indents(1) + "}\n" +
      indents(1) + "catch (Exception)\n" +
      indents(1) + "{}\n" +
      indents(1) + "Thread.Sleep(1000);\n" +
      "}";
}

function assertOrVerifyFailure(line, isAssert) {
  var message = '"expected failure"';
  var failStatement = isAssert ? "Assert.Fail(" + message + ");" :
      "verificationErrors.Append(" + message + ");";
  return "try\n" +
      "{\n" +
      line + "\n" +
      failStatement + "\n" +
      "}\n" +
      "catch (Exception) {}\n";
}

function pause(milliseconds) {
  return "Thread.Sleep(" + parseInt(milliseconds, 10) + ");";
}

function echo(message) {
  return "Console.WriteLine(" + xlateArgument(message) + ");";
}

function formatComment(comment) {
  return comment.comment.replace(/.+/mg, function(str) {
    return "// " + str;
  });
}

/**
 * Returns a string representing the suite for this formatter language.
 *
 * @param testSuite  the suite to format
 * @param filename   the file the formatted suite will be saved as
 */
function formatSuite(testSuite, filename) {
  var suiteClass = /^(\w+)/.exec(filename)[1];
  suiteClass = suiteClass[0].toUpperCase() + suiteClass.substring(1);

  var formattedSuite = "using NUnit.Framework;\n"
      + "using NUnit.Core;\n"
      + "\n"
      + "namespace " + this.options.namespace + "\n"
      + '{\n'
      + indents(1) + "public class " + suiteClass + "\n"
      + indents(1) + '{\n'
      + indents(2) + "[Suite] public static TestSuite Suite\n"
      + indents(2) + '{\n'
      + indents(3) + "get\n"
      + indents(3) + '{\n'
      + indents(4) + 'TestSuite suite = new TestSuite("'+ suiteClass +'");\n';

  for (var i = 0; i < testSuite.tests.length; ++i) {
    var testClass = testSuite.tests[i].getTitle();
    formattedSuite += indents(4)
        + "suite.Add(new " + testClass + "());\n";
  }

  formattedSuite += indents(4) + "return suite;\n"
      + indents(3) + "}\n"
      + indents(2) + "}\n"
      + indents(1) + "}\n"
      + "}\n";

  return formattedSuite;
}

function defaultExtension() {
  return this.options.defaultExtension;
}

this.options = {
  receiver: "driver",
  showSelenese: 'false',
  namespace: "SeleniumTests",
  indent: '4',
  initialIndents:  '3',
  header:
          'using System;\n' +
	  'using System.Linq;\n'+
          'using System.Text;\n' +
          'using System.Text.RegularExpressions;\n' +
          'using System.Threading;\n' +
          'using NUnit.Framework;\n' +
          'using OpenQA.Selenium;\n' +
          'using OpenQA.Selenium.Firefox;\n' +
          'using OpenQA.Selenium.Support.UI;\n' +
          '\n' +
          'namespace ${namespace}\n' +
          '{\n' +
          '    [TestFixture]\n' +
          '    public class ${className}\n' +
          '    {\n' +
          '        private IWebDriver driver;\n' +
          '        private StringBuilder verificationErrors;\n' +
          '        private string baseURL;\n' +
          "        private bool acceptNextAlert = true;\n" +
          '        \n' +
          '        [SetUp]\n' +
          '        public void SetupTest()\n' +
          '        {\n' +
          '            ${receiver} = new FirefoxDriver();\n' +
          '            baseURL = "${baseURL}";\n' +
          '            verificationErrors = new StringBuilder();\n' +
          '        }\n' +
          '        \n' +
          '        [TearDown]\n' +
          '        public void TeardownTest()\n' +
          '        {\n' +
          '            try\n' +
          '            {\n' +
          '                ${receiver}.Quit();\n' +
          '            }\n' +
          '            catch (Exception)\n' +
          '            {\n' +
          '                // Ignore errors if unable to close the browser\n' +
          '            }\n' +
          '            Assert.AreEqual("", verificationErrors.ToString());\n' +
          '        }\n' +
          '        \n' +
          '        [Test]\n' +
          '        public void ${methodName}()\n' +
          '        {\n'+
          '            var mainWindow=driver.CurrentWindowHandle;\n' ,
  footer:
          '        }\n' +
          "        private bool IsElementPresent(By by)\n" +
          "        {\n" +
          "            try\n" +
          "            {\n" +
          "                driver.FindElement(by);\n" +
          "                return true;\n" +
          "            }\n" +
          "            catch (NoSuchElementException)\n" +
          "            {\n" +
          "                return false;\n" +
          "            }\n" +
          "        }\n" +
          '        \n' +
  	      "        private void windowSwitch(string title)\n" +
          "        {\n" +
          "            var windows = driver.WindowHandles;\n"+
          "            foreach (var window in windows)\n" +
          "                if (driver.SwitchTo().Window(window).Title == title)\n"+
          "                    return;\n"+
		      "            Assert.Fail(\"Cannot find window: \"+title);\n"+
          "        }\n" +
          '        \n' +
          "        private bool IsAlertPresent()\n" +
          "        {\n" +
          "            try\n" +
          "            {\n" +
          "                driver.SwitchTo().Alert();\n" +
          "                return true;\n" +
          "            }\n" +
          "            catch (NoAlertPresentException)\n" +
          "            {\n" +
          "                return false;\n" +
          "            }\n" +
          "        }\n" +
          '        \n' +
      	  '        private void waitForPopup(string title="null", int waitTime=20000)\n'+
          "        {\n" +
    		  '             waitTime = waitTime / 1000;\n'+
    		  '             if (title == "null" || title.Length == 0)\n'+
    		  "             {\n"+
    		  "                int windowNum = driver.WindowHandles.Count;\n"+
    		  "                for (int second = 0; second < 20; second++){\n" +
    		  "                    Thread.Sleep(1000);\n"+
    		  "                    if (driver.WindowHandles.Count > 1) break;//temporary\n" +
    		  '                }\n'+
    		  '                if (driver.WindowHandles.Count == 1)\n'+
    		  '                    Assert.Fail("timeout waiting for popup");\n'+
    		  "             }\n"+
		  "             else\n"+
		  "             {\n"+
		  "                for (int second = 0; second < waitTime; second++)\n"+
		  "                   {\n"+
		  "                      var windows = driver.WindowHandles;\n"+
		  "                      foreach (var window in windows)\n"+
		  "                         if (driver.SwitchTo().Window(window).Title == title)\n"+
		  "                             return;\n"+
		  "                      Thread.Sleep(1000);\n"+
		  "                   }\n"+
		  '                   Assert.Fail("timeout waiting for popup \\""+title+"\\"");\n'+
		  "             }\n"+
    		  "        }\n"+
          '        \n' +
          "        private string CloseAlertAndGetItsText() {\n" +
          "            try {\n" +
          "                IAlert alert = driver.SwitchTo().Alert();\n" +
          "                string alertText = alert.Text;\n" +
          "                if (acceptNextAlert) {\n" +
          "                    alert.Accept();\n" +
          "                } else {\n" +
          "                    alert.Dismiss();\n" +
          "                }\n" +
          "                return alertText;\n" +
          "            } finally {\n" +
          "                acceptNextAlert = true;\n" +
          "            }\n" +
          "        }\n" +
          '    }\n' +
          '}\n',
  defaultExtension: "cs"
};
this.configForm = '<description>Variable for Selenium instance</description>' +
    '<textbox id="options_receiver" />' +
    '<description>Namespace</description>' +
    '<textbox id="options_namespace" />' +
    '<checkbox id="options_showSelenese" label="Show Selenese"/>';

this.name = "C# (WebDriver)";
this.testcaseExtension = ".cs";
this.suiteExtension = ".cs";
this.webdriver = true;

WDAPI.Driver = function() {
  this.ref = options.receiver;
};

WDAPI.Driver.searchContext = function(locatorType, locator) {
  var locatorString = xlateArgument(locator);
  switch (locatorType) {
    case 'xpath':
      return 'By.XPath(' + locatorString + ')';
    case 'css':
      return 'By.CssSelector(' + locatorString + ')';
    case 'id':
      return 'By.Id(' + locatorString + ')';
    case 'link':
      return 'By.LinkText(' + locatorString + ')';
    case 'name':
      return 'By.Name(' + locatorString + ')';
    case 'tag_name':
      return 'By.TagName(' + locatorString + ')';
  }
  throw 'Error: unknown strategy [' + locatorType + '] for locator [' + locator + ']';
};

WDAPI.Driver.prototype.back = function() {
  return this.ref + ".Navigate().Back()";
};

WDAPI.Driver.prototype.close = function() {
  return this.ref + ".Close()";
};

WDAPI.Driver.prototype.findElement = function(locatorType, locator) {
  return new WDAPI.Element(this.ref + ".FindElement(" + WDAPI.Driver.searchContext(locatorType, locator) + ")");
};

WDAPI.Driver.prototype.findElements = function(locatorType, locator) {
  return new WDAPI.ElementList(this.ref + ".FindElements(" + WDAPI.Driver.searchContext(locatorType, locator) + ")");
};

WDAPI.Driver.prototype.getCurrentUrl = function() {
  return this.ref + ".Url";
};

WDAPI.Driver.prototype.get = function(url) {
  if (url.length > 1 && (url.substring(1,8) == "http://" || url.substring(1,9) == "https://")) { // url is quoted
    return this.ref + ".Navigate().GoToUrl(" + url + ")";
  } else {
    return this.ref + ".Navigate().GoToUrl(baseURL + " + url + ")";
  }
};

WDAPI.Driver.prototype.getTitle = function() {
  return this.ref + ".Title";
};

WDAPI.Driver.prototype.getAlert = function() {
  return "CloseAlertAndGetItsText()";
};

WDAPI.Driver.prototype.chooseOkOnNextConfirmation = function() {
  return "acceptNextAlert = true";
};

WDAPI.Driver.prototype.chooseCancelOnNextConfirmation = function() {
  return "acceptNextAlert = false";
};

WDAPI.Driver.prototype.refresh = function() {
  return this.ref + ".Navigate().Refresh()";
};

WDAPI.Element = function(ref) {
  this.ref = ref;
};

WDAPI.Element.prototype.clear = function() {
  return this.ref + ".Clear()";
};

WDAPI.Element.prototype.click = function() {
  return this.ref + ".Click()";
};

WDAPI.Element.prototype.getAttribute = function(attributeName) {
  return this.ref + ".GetAttribute(" + xlateArgument(attributeName) + ")";
};

WDAPI.Element.prototype.getText = function() {
  return this.ref + ".Text";
};

WDAPI.Element.prototype.isDisplayed = function() {
  return this.ref + ".Displayed";
};

WDAPI.Element.prototype.isSelected = function() {
  return this.ref + ".Selected";
};

WDAPI.Element.prototype.sendKeys = function(text) {
  return this.ref + ".SendKeys(" + xlateArgument(text) + ")";
};

WDAPI.Element.prototype.submit = function() {
  return this.ref + ".Submit()";
};

WDAPI.Element.prototype.select = function(selectLocator) {
  if (selectLocator.type == 'index') {
    return "new SelectElement(" + this.ref + ").SelectByIndex(" + selectLocator.string + ")";
  }
  if (selectLocator.type == 'value') {
    return "new SelectElement(" + this.ref + ").SelectByValue(" + xlateArgument(selectLocator.string) + ")";
  }
  return "new Select(" + this.ref + ").SelectByText(" + xlateArgument(selectLocator.string) + ")";
};

WDAPI.Element.prototype.deselect = function(selectLocator) {
  if (selectLocator.type == 'index') {
    return "new SelectElement(" + this.ref + ").DeselectByIndex(" + selectLocator.string + ")";
  }
  if (selectLocator.type == 'value') {
    return "new SelectElement(" + this.ref + ").DeselectByValue(" + xlateArgument(selectLocator.string) + ")";
  }
  return "new SelectElement(" + this.ref + ").DeselectByText(" + xlateArgument(selectLocator.string) + ")";
};

WDAPI.ElementList = function(ref) {
  this.ref = ref;
};

WDAPI.ElementList.prototype.getItem = function(index) {
  return this.ref + "[" + index + "]";
};

WDAPI.ElementList.prototype.getSize = function() {
  return this.ref + ".Count";
};

WDAPI.ElementList.prototype.isEmpty = function() {
  return this.ref + ".Count == 0";
};

WDAPI.Element.prototype.contextMenu = function() {
  return "new Actions(driver).ContextClick("+this.ref+").Perform()";
};

WDAPI.Driver.prototype.switchWindow = function(name) {
  if(name=="null")
    return this.ref + ".SwitchTo().Window(mainWindow)";
  if(name=="last")
	  return "windows=driver.WindowHandles;\n"+this.ref + ".SwitchTo().Window(windows[windows.Count-1])";
  return "windowSwitch("+xlateArgument(name.split("=")[1])+")";
};

WDAPI.Driver.prototype.selectPopup = function(name) {
  if(name=="null")
	  return this.ref + ".SwitchTo().Window(driver.WindowHandles[driver.WindowHandles.Count-1])";
  if(name=="")
	  return this.ref + ".SwitchTo().Window(driver.WindowHandles[driver.WindowHandles.Count-1])";
  return "windowSwitch("+xlateArgument(name.split("=")[1])+")";
};

WDAPI.Driver.prototype.switchFrame = function(name) {
  if(name.split("=")[0]=="index")
		return "driver.SwitchTo().Frame("+name.split("=")[1]+")";
  return this.ref + ".SwitchTo().Frame("+xlateArgument(name)+")";
};

WDAPI.Element.prototype.SelectedOption = function() {
  return new WDAPI.Element("new SelectElement("+this.ref + ").SelectedOption");
};

WDAPI.Element.prototype.location = function() {
  return this.ref+".Location";
};

WDAPI.Element.prototype.getElementPositionTop = function() {
  return this.ref+".Location.Y";
};

WDAPI.Element.prototype.MoveToElement = function() {
  return "new Actions(driver).MoveToElement("+this.ref+").Perform()";
};

//in webdriver this uses coordinates
WDAPI.Element.prototype.mouseDown = function() {
   return "new Actions(driver).ClickAndHold("+this.ref+").Perform()";
};

WDAPI.Element.prototype.mouseUp = function() {
   return "new Actions(driver).Release("+this.ref+").Perform()";
};


WDAPI.Element.prototype.dragAndDrop = function(Destination) {
  return "new Actions(driver).DragAndDrop("+this.ref+","+Destination.ref+").Perform()";
};

WDAPI.Element.prototype.isEditable = function() {
  return this.ref+".Enabled";
};

WDAPI.Element.prototype.doubleClick = function() {
  return "new Actions(driver).DoubleClick("+this.ref+").Perform()";
};

WDAPI.Element.prototype.keyPress = function(keyPressed) {
   if(keyPressed.indexOf("\\")!=-1)
   {
      keyPressed= keyPressed.substring(1,keyPressed.length);
      return this.ref+".SendKeys(\"\"+Convert.ToChar("+keyPressed+"))";
   }
  return this.ref+".SendKeys(\""+keyPressed+"\")";
};

WDAPI.Element.prototype.dragAndDropOffset = function(offSet) {
  return "new Actions(driver).DragAndDropToOffset("+this.ref+","+offSet[0]+","+offSet[1]+").Perform()";
};

WDAPI.Utils = function() {
};

WDAPI.Utils.isElementPresent = function(how, what) {
  return "IsElementPresent(" + WDAPI.Driver.searchContext(how, what) + ")";
};

WDAPI.Utils.isAlertPresent = function() {
  return "IsAlertPresent()";
};

WDAPI.Utils.getAllWindowTitles = function(arg) {
  return "driver.WindowHandles.Select(x => driver.SwitchTo().Window(x).Title).ToArray()";
};

WDAPI.Utils.waitForPopup = function() {
   if(name[1].length>1)
      return 'waitForPopup("'+name[0]+'",'+name[1]+')';
   return 'waitForPopup("'+name+'")';
};

WDAPI.Utils.getEval = function(evalu) {
   return "(String)((IJavaScriptExecutor)driver).ExecuteScript("+xlateArgument(javaScriptFix(evalu))+")";
};

//Phase 2/6/2013 - Simply adds a return to javascript statements
function javaScriptFix(sentence){
  if(sentence.indexOf("return")!=-1)
		return sentence;
	var withoutSemi;
	if(sentence.trim()[sentence.length-1]==";")
		withoutSemi = sentence.trim().substring(0,sentence.length-1);
	else
		withoutSemi = sentence.trim();
	var splitz = withoutSemi.split(';');
	splitz[splitz.length-1]= "return ("+splitz[splitz.length-1]+'+"")';
	return splitz.join(';')+';';
};
