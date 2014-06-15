/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package falserunner;

import java.util.*;

/**
 *
 * @author m1kc
 */
public class FalseRunner
{
    Stack stack = new Stack();
    Hashtable vars = new Hashtable();
    String output = "";
    String stackInfo = "";
    String variablesInfo = "";

    final static int TRUE = -1;
    final static int FALSE = 0;

    public void run(String code)
    {
        // Выпиливаем комментарии
        // Регулярное выражение: {любой текст}
        // (точка - любой символ, * - любое число повторений,
        // знак вопроса - искать максимально короткую строку (ленивый поиск)).
        code = code.replaceAll("[{].*?[}]", "");
        // Run
        eval(code);
        // End
        for (int i=0; i<stack.size(); i++)
        {
            Object o = stack.get(i);
            if (o.getClass()==Integer.class) 
            {
                int z = ((Integer) o).intValue();
                stackInfo+=z;
                if (z==FALSE) stackInfo+="(false)";
                if (z==TRUE) stackInfo+="(true)";
            }
            if (o.getClass()==String.class) stackInfo+=o.toString();
            stackInfo+=" ";
        }
        for (char i='a'; i<='z'; i++) 
            if (vars.containsKey(""+i))
                variablesInfo+=(i+" = "+vars.get(""+i).toString()+"\n");
    }

    private void eval(String code)
    {
        System.out.println("eval: "+code);
        int n = 0;
        boolean toNew = true;
        boolean printing = false;
        int putFunc = 0;
        String func = "";
        boolean putChar = false;
        // Run
        while(n<code.length())
        {
            char current = code.charAt(n);

            // Если не цифра - заканчиваем ввод числа
            if (!(current>='0' && current<='9')) toNew = true;
            
            // Если квадратная скобка
            if (current=='[')
            {
                putFunc++;
                if (putFunc==1)
                {
                    func = "";
                }
                else
                {
                    func+='[';
                }
            }
            else
            if (current==']')
            {
                putFunc--;
                if (putFunc==0)
                {
                    stack.addElement(func);
                }
                else
                {
                    func+=']';
                }
            }
            else
            // Если собираем функцию
            if (putFunc>0) func+=current;
            else
            // Если кавычка, включаем или выключаем вывод строки
            if (current=='"') printing = !printing;
            else
            // Если выводим строку
            if (printing) print(current);
            else
            // Если апостроф
            if (current=='\'') putChar = true;
            else
            // Если после апострофа
            if (putChar)
            {
                put(current);
                putChar = false;
            }
            else
            // Если цифра
            if (current>='0' && current<='9')
            {
                if (toNew)
                {
                    put(current-'0');
                    toNew = false;
                }
                else
                {
                    if (stack.isEmpty())
                    {
                        put(current-'0');
                    }
                    else
                    {
                        int i = extract();
                        i*=10;
                        i+=(current-'0');
                        put(i);
                    }
                }
            }
            else
            // Если буква
            if (current>='a' && current<='z')
            {
                stack.addElement(""+current);
            }
            else
            // Если команда
            switch(current)
            {
                case ' ':
                    //toNew = true;
                    break;
                case '\n':
                    // Игнорировать
                    break;
                case '+':
                    put(extract()+extract());
                    break;
                case '-':
                    put(-extract()+extract());
                    break;
                case '*':
                    put(extract()*extract());
                    break;
                case '/':
                {
                    int b = extract();
                    int a = extract();
                    put(a/b);
                }
                    break;
                case '_': // Унарный минус
                    put(-extract());
                    break;
                case '=':
                    put(extract()==extract() ? TRUE:FALSE);
                    break;
                case '>':
                    put(extract()<extract() ? TRUE:FALSE);
                    break;
                case '<':
                    put(extract()>extract() ? TRUE:FALSE);
                    break;
                case '&':
                    put(extract() & extract());
                    break;
                case '|':
                    put(extract() | extract());
                    break;
                case '~': // Логическое отрицание
                    put(extract()==TRUE ? FALSE:TRUE);
                    break;
                case '$': // Дублирование вершины стека
                    put(peek());
                    break;
                case '%': // Удаление вершины стека
                    delete();
                    break;
                case '\\': // Меняет местами два верхних элемента
                {
                    int b = extract();
                    int a = extract();
                    put(b); put(a);
                }
                    break;
                case '@': // Циклическая перестановка трех верхних элементов
                {
                    int c = extract();
                    int b = extract();
                    int a = extract();
                    put(b); put(c); put(a);
                }
                    break;
                case 'ш': // Вытаскивает из стека элемент, номер которого находится на вершине стека
                    put(get(extract()));
                    break;
                case '?': // if
                {
                    String f = stack.pop().toString();
                    if (extract()==TRUE) eval(f);
                }
                    break;
                case ':': // Присваивание
                {
                    String name = stack.pop().toString();
                    vars.put(name, stack.pop());
                }
                    break;
                case ';': // Вытащить значение переменной
                {
                    String name = stack.pop().toString();
                    stack.addElement(vars.get(name));
                }
                    break;
                case '!': // Выполнить функцию
                    eval(stack.pop().toString());
                    break;
                case '#': // while
                {
                    String f = stack.pop().toString();
                    String w = stack.pop().toString();
                    while(true)
                    {
                        eval(w); if (extract()==TRUE) eval(f); else break;
                    }
                }
                    break;
                case '.': // Вывод числа
                    print(""+extract());
                    break;
                case ',': // Вывод буквы
                    print((char)extract());
                    break;
                default:
                    System.out.println("Неожиданный символ: "+current+" на позиции "+n);
                    break;
            }

            n++;
        }
    }

    private int extract()
    {
        return ((Integer)stack.pop()).intValue();
    }

    private void put(int n)
    {
        stack.addElement(new Integer(n));
    }

    private int peek()
    {
        return ((Integer)stack.peek()).intValue();
    }

    private void delete()
    {
        stack.pop();
    }

    private int get(int i)
    {
        return ((Integer)stack.elementAt(stack.size()-i-1)).intValue();
    }

    /*
    private void set(int number, int index)
    {
        stack.setElementAt(new Integer(number), index);
    }
     */

    private void print(String s)
    {
        output += s;
    }

    private void print(char s)
    {
        output += s;
    }

    public String getOutput()
    {
        return output;
    }

    public String getStack()
    {
        return stackInfo;
    }

    public String getVariables()
    {
        return variablesInfo;
    }

}
