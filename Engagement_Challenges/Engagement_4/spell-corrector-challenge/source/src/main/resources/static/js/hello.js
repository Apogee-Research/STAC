function get_form(element) {
  while (element) {
    element = element.parentNode;
    if (element.tagName.toLowerCase() == "form") {
      //alert( element ) //debug/test
      return element
    }
  }
  return 0; //error: no form found in ancestors
}