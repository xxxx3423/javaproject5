import java.io.*;
import java.util.*;

interface Displayable {
    void display();
}

interface Command {
    void execute();
    void undo();
}

class AddPersonCommand implements Command {
    private List<Person> personsList;
    private Person person;

    public AddPersonCommand(List<Person> personsList, Person person) {
        this.personsList = personsList;
        this.person = person;
    }

    public void execute() {
        personsList.add(person);
        CommandHistory.getInstance().addCommand(this);
    }

    public void undo() {
        personsList.remove(person);
    }
}

class MacroCommand implements Command {
    private List<Command> commands = new ArrayList<>();
    
    public void addCommand(Command command) {
        commands.add(command);
    }
    
    public void execute() {
        for (Command command : commands) {
            command.execute();
        }
    }
    
    public void undo() {
        for (int i = commands.size() - 1; i >= 0; i--) {
            commands.get(i).undo();
        }
    }
}

class CommandHistory {
    private static CommandHistory instance;
    private Stack<Command> history = new Stack<>();

    private CommandHistory() {}

    public static CommandHistory getInstance() {
        if (instance == null) {
            instance = new CommandHistory();
        }
        return instance;
    }

    public void addCommand(Command command) {
        history.push(command);
    }

    public void undoLastCommand() {
        if (!history.isEmpty()) {
            history.pop().undo();
        }
    }
}

class Person implements Serializable, Displayable {
    private static final long serialVersionUID = 1L;
    protected String name;
    protected int age;
    protected transient String password;

    public Person(String name, int age, String password) {
        this.name = name;
        this.age = age;
        this.password = password;
    }

    public void display() {
        System.out.printf("| %-15s | %-3d |\n", name, age);
    }
}

class Employee extends Person {
    private String position;

    public Employee(String name, int age, String password, String position) {
        super(name, age, password);
        this.position = position;
    }

    public void display() {
        System.out.printf("| %-15s | %-3d | %-10s |\n", name, age, position);
    }
}

public class SerializationWithUndo {
    private static final String FILE_NAME = "persons.ser";
    private static List<Person> personsList = new ArrayList<>();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            System.out.println("1. Додайте особу\n2. Скасувати\n3. Зберегти\n4. Завантажити\n5. Дисплей\n6. Вихід");
            System.out.print("Виберіть варіант: ");
            int choice = scanner.nextInt();
            scanner.nextLine();
            
            switch (choice) {
                case 1:
                    addPerson();
                    break;
                case 2:
                    CommandHistory.getInstance().undoLastCommand();
                    break;
                case 3:
                    saveToFile();
                    break;
                case 4:
                    loadFromFile();
                    break;
                case 5:
                    displayPersons();
                    break;
                case 6:
                    return;
                default:
                    System.out.println("Invalid option!");
            }
        }
    }

    private static void addPerson() {
        System.out.print("Enter name: ");
        String name = scanner.nextLine();
        System.out.print("Enter age: ");
        int age = scanner.nextInt();
        scanner.nextLine();
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        System.out.print("Is this person an employee? (yes/no): ");
        String response = scanner.nextLine();

        Person person;
        if (response.equalsIgnoreCase("yes")) {
            System.out.print("Enter position: ");
            String position = scanner.nextLine();
            person = new Employee(name, age, password, position);
        } else {
            person = new Person(name, age, password);
        }

        Command command = new AddPersonCommand(personsList, person);
        command.execute();
    }

    private static void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(personsList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            personsList = (List<Person>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void displayPersons() {
        System.out.println("--------------------------------------");
        personsList.forEach(Person::display);
        System.out.println("--------------------------------------");
    }
}