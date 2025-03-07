import java.io.*;
import java.util.*;

// Абстрактний клас для обчислень
abstract class ComputationData implements Serializable {
    private static final long serialVersionUID = 1L;
    protected double param1;
    protected double param2;
    protected double result;

    public ComputationData(double param1, double param2) {
        this.param1 = param1;
        this.param2 = param2;
    }

    public abstract void compute();
    public double getResult() { return result; }

    public void compute(double additionalValue) {
        this.result += additionalValue;
    }
}

// Операції
class AdditionComputation extends ComputationData {
    public AdditionComputation(double param1, double param2) { super(param1, param2); }
    public void compute() { result = param1 + param2; }
}

class SubtractionComputation extends ComputationData {
    public SubtractionComputation(double param1, double param2) { super(param1, param2); }
    public void compute() { result = param1 - param2; }
}

class MultiplicationComputation extends ComputationData {
    public MultiplicationComputation(double param1, double param2) { super(param1, param2); }
    public void compute() { result = param1 * param2; }
}

class DivisionComputation extends ComputationData {
    public DivisionComputation(double param1, double param2) { super(param1, param2); }
    public void compute() { result = (param2 != 0) ? param1 / param2 : Double.NaN; }
}

// Інтерфейс команди
interface Command {
    void execute();
    void undo();
    double getResult();
}

// Абстрактна команда для обчислень
abstract class ComputationCommand implements Command {
    protected ComputationData computation;
    protected double result;

    public ComputationCommand(ComputationData computation) {
        this.computation = computation;
    }

    public void execute() {
        computation.compute();
        result = computation.getResult();
    }

    public void undo() {
        computation.compute(-result);
    }
    
    public double getResult() {
        return result;
    }
}

// Конкретні команди
class AdditionCommand extends ComputationCommand {
    public AdditionCommand(double param1, double param2) {
        super(new AdditionComputation(param1, param2));
    }
}

class SubtractionCommand extends ComputationCommand {
    public SubtractionCommand(double param1, double param2) {
        super(new SubtractionComputation(param1, param2));
    }
}

class MultiplicationCommand extends ComputationCommand {
    public MultiplicationCommand(double param1, double param2) {
        super(new MultiplicationComputation(param1, param2));
    }
}

class DivisionCommand extends ComputationCommand {
    public DivisionCommand(double param1, double param2) {
        super(new DivisionComputation(param1, param2));
    }
}

// Макрокоманда
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
    
    public double getResult() {
        return 0; // Не має одного значення
    }
}

// Менеджер команд (Singleton)
class CommandManager {
    private static CommandManager instance;
    private Stack<Command> history = new Stack<>();
    private List<Double> results = new ArrayList<>();

    private CommandManager() {}

    public static CommandManager getInstance() {
        if (instance == null) {
            instance = new CommandManager();
        }
        return instance;
    }

    public void executeCommand(Command command) {
        command.execute();
        history.push(command);
        results.add(command.getResult());
        displayResults();
    }

    public void undoLastCommand() {
        if (!history.isEmpty()) {
            Command lastCommand = history.pop();
            lastCommand.undo();
            results.remove(results.size() - 1);
            System.out.println("Операція скасована!");
            displayResults();
        } else {
            System.out.println("Немає команд для скасування!");
        }
    }

    private void displayResults() {
        System.out.println("Поточні результати: " + results);
    }
}

// Основний клас
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        CommandManager manager = CommandManager.getInstance();

        while (true) {
            System.out.println("Оберіть операцію: +, -, *, / або undo для скасування");
            String operation = scanner.next();

            if (operation.equals("undo")) {
                manager.undoLastCommand();
                continue;
            }

            System.out.println("Введіть два числа:");
            double param1 = scanner.nextDouble();
            double param2 = scanner.nextDouble();
            
            Command command;
            switch (operation) {
                case "+": command = new AdditionCommand(param1, param2); break;
                case "-": command = new SubtractionCommand(param1, param2); break;
                case "*": command = new MultiplicationCommand(param1, param2); break;
                case "/": command = new DivisionCommand(param1, param2); break;
                default: System.out.println("Невідома операція"); continue;
            }
            
            manager.executeCommand(command);
        }
    }
}