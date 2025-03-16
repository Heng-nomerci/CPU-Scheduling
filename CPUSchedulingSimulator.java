import java.util.*;

class Process {
    String id;
    int arrivalTime, burstTime, waitingTime, turnaroundTime, remainingTime, completionTime;

    public Process(String id, int arrivalTime, int burstTime) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.waitingTime = 0;
        this.turnaroundTime = 0;
        this.completionTime = 0;
    }
}

class CPUSchedulingSimulator {
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            clearScreen();
            System.out.println("\nCPU Scheduling Simulator");
            System.out.println("1. First-Come, First-Served (FCFS)");
            System.out.println("2. Shortest-Job-First (SJF)");
            System.out.println("3. Shortest-Remaining-Time (SRT)");
            System.out.println("4. Round Robin (RR)");
            System.out.println("5. Exit");

            int choice;
            try {
                System.out.print("Enter your choice (1-5): ");
                choice = Integer.parseInt(scanner.nextLine());
                if (choice == 5) {
                    System.out.println("Exiting program...");
                    break;
                }
                if (choice < 1 || choice > 5) {
                    System.out.println("Invalid choice. Press Enter to continue...");
                    scanner.nextLine();
                    continue;
                }

                List<Process> processes = getProcesses();
                switch (choice) {
                    case 1:
                        fcfs(processes);
                        break;
                    case 2:
                        sjf(processes);
                        break;
                    case 3:
                        srt(processes);
                        break;
                    case 4:
                        roundRobin(processes);
                        break;
                    default:
                        System.out.println("Invalid choice. Try again.");
                        continue;
                }

                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer. Press Enter to continue...");
                scanner.nextLine(); // Clear the invalid input
            }
        }
        scanner.close();
    }

    private static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static List<Process> getProcesses() {
        int n;
        while (true) {
            try {
                System.out.print("Enter number of processes: ");
                n = Integer.parseInt(scanner.nextLine());
                if (n <= 0) {
                    System.out.println("Please enter a positive number.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }

        List<Process> processes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            while (true) {
                try {
                    System.out.print("Enter Process ID, Arrival Time, Burst Time (e.g., P1 0 5): ");
                    String[] input = scanner.nextLine().trim().split("\\s+");
                    if (input.length != 3) {
                        System.out.println("Please provide exactly 3 values (ID, Arrival Time, Burst Time).");
                        continue;
                    }
                    String id = input[0];
                    int arrival = Integer.parseInt(input[1]);
                    int burst = Integer.parseInt(input[2]);
                    if (arrival < 0 || burst <= 0) {
                        System.out.println("Arrival time must be non-negative and burst time must be positive.");
                        continue;
                    }
                    processes.add(new Process(id, arrival, burst));
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("Arrival and Burst times must be integers.");
                } catch (Exception e) {
                    System.out.println("Invalid input format. Try again.");
                }
            }
        }
        return processes;
    }

    private static void fcfs(List<Process> processes) {
        Collections.sort(processes, new Comparator<Process>() {
            public int compare(Process p1, Process p2) {
                return Integer.compare(p1.arrivalTime, p2.arrivalTime);
            }
        });
        int currentTime = 0;
        StringBuilder ganttChart = new StringBuilder("Gantt Chart: ");

        for (Process p : processes) {
            if (currentTime < p.arrivalTime) currentTime = p.arrivalTime;
            if (ganttChart.length() > 12) ganttChart.append(" → "); // Start after "Gantt Chart: "
            ganttChart.append(p.id).append(" (").append(currentTime).append("–");
            p.waitingTime = currentTime - p.arrivalTime;
            currentTime += p.burstTime;
            p.turnaroundTime = p.waitingTime + p.burstTime;
            p.completionTime = currentTime;
            ganttChart.append(currentTime).append(")");
        }

        System.out.println(ganttChart.toString());
        printResults(processes, "FCFS");
    }

    private static void sjf(List<Process> processes) {
        List<Process> copy = new ArrayList<>(processes);
        Collections.sort(copy, new Comparator<Process>() {
            public int compare(Process p1, Process p2) {
                return Integer.compare(p1.arrivalTime, p2.arrivalTime);
            }
        });
        int currentTime = 0;
        StringBuilder ganttChart = new StringBuilder("Gantt Chart: ");
        List<Process> completed = new ArrayList<>();

        while (!copy.isEmpty()) {
            List<Process> available = new ArrayList<>();
            for (Process p : copy) {
                if (p.arrivalTime <= currentTime) available.add(p);
            }
            if (available.isEmpty()) {
                currentTime = copy.get(0).arrivalTime;
                continue;
            }

            Process shortest = available.get(0);
            for (Process p : available) {
                if (p.burstTime < shortest.burstTime) shortest = p;
            }
            copy.remove(shortest);

            if (ganttChart.length() > 12) ganttChart.append(" → "); // Start after "Gantt Chart: "
            ganttChart.append(shortest.id).append(" (").append(currentTime).append("–");
            shortest.waitingTime = currentTime - shortest.arrivalTime;
            currentTime += shortest.burstTime;
            shortest.turnaroundTime = currentTime - shortest.arrivalTime;
            shortest.completionTime = currentTime;
            ganttChart.append(currentTime).append(")");
            completed.add(shortest);
        }

        processes.clear();
        processes.addAll(completed);
        System.out.println(ganttChart.toString());
        printResults(processes, "SJF");
    }

    private static void srt(List<Process> processes) {
        int currentTime = 0;
        int completed = 0;
        StringBuilder ganttChart = new StringBuilder("Gantt Chart: ");
        Process lastProcess = null;
        int lastStartTime = 0;

        while (completed < processes.size()) {
            Process shortest = null;
            for (Process p : processes) {
                if (p.arrivalTime <= currentTime && p.remainingTime > 0) {
                    if (shortest == null || p.remainingTime < shortest.remainingTime) {
                        shortest = p;
                    }
                }
            }
            if (shortest == null) {
                currentTime++;
                continue;
            }

            if (shortest != lastProcess && lastProcess != null) {
                ganttChart.append(lastProcess.id).append(" (").append(lastStartTime).append("–").append(currentTime).append(")");
                ganttChart.append(" → ");
                lastStartTime = currentTime;
            } else if (lastProcess == null) {
                lastStartTime = currentTime;
            }

            shortest.remainingTime--;
            currentTime++;

            if (shortest.remainingTime == 0) {
                completed++;
                shortest.completionTime = currentTime;
                shortest.turnaroundTime = shortest.completionTime - shortest.arrivalTime;
                shortest.waitingTime = shortest.turnaroundTime - shortest.burstTime;
                ganttChart.append(shortest.id).append(" (").append(lastStartTime).append("–").append(currentTime).append(")");
                if (completed < processes.size()) ganttChart.append(" → ");
            }
            lastProcess = shortest;
        }

        System.out.println(ganttChart.toString());
        printResults(processes, "SRT");
    }

    private static void roundRobin(List<Process> processes) {
        int quantum;
        while (true) {
            try {
                System.out.print("Enter Time Quantum: ");
                quantum = Integer.parseInt(scanner.nextLine());
                if (quantum <= 0) {
                    System.out.println("Quantum must be positive.");
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }

        Collections.sort(processes, new Comparator<Process>() {
            public int compare(Process p1, Process p2) {
                return Integer.compare(p1.arrivalTime, p2.arrivalTime);
            }
        });
        Queue<Process> queue = new LinkedList<>();
        int currentTime = processes.get(0).arrivalTime;
        StringBuilder ganttChart = new StringBuilder("Gantt Chart: ");
        int index = 0;
        Process lastProcess = null;
        int lastStartTime = currentTime;

        while (completedCount(processes) < processes.size()) {
            while (index < processes.size() && processes.get(index).arrivalTime <= currentTime) {
                queue.add(processes.get(index));
                index++;
            }

            if (queue.isEmpty()) {
                currentTime++;
                continue;
            }

            Process p = queue.poll();
            if (p.arrivalTime > currentTime) {
                queue.add(p);
                currentTime++;
                continue;
            }

            if (lastProcess != null && lastProcess != p) {
                ganttChart.append(lastProcess.id).append(" (").append(lastStartTime).append("–").append(currentTime).append(")");
                ganttChart.append(" → ");
                lastStartTime = currentTime;
            } else if (lastProcess == null) {
                lastStartTime = currentTime;
            }
            lastProcess = p;

            int executeTime = Math.min(quantum, p.remainingTime);
            p.remainingTime -= executeTime;
            currentTime += executeTime;

            if (p.remainingTime == 0) {
                p.completionTime = currentTime;
                p.turnaroundTime = p.completionTime - p.arrivalTime;
                p.waitingTime = p.turnaroundTime - p.burstTime;
            }

            while (index < processes.size() && processes.get(index).arrivalTime <= currentTime) {
                queue.add(processes.get(index));
                index++;
            }

            if (p.remainingTime > 0) {
                queue.add(p);
            } else if (lastProcess != null) {
                ganttChart.append(p.id).append(" (").append(lastStartTime).append("–").append(currentTime).append(")");
                if (!queue.isEmpty()) ganttChart.append(" → ");
            }
        }

        System.out.println(ganttChart.toString());
        printResults(processes, "Round Robin");
    }

    private static int completedCount(List<Process> processes) {
        int count = 0;
        for (Process p : processes) {
            if (p.remainingTime == 0) count++;
        }
        return count;
    }

    private static void printResults(List<Process> processes, String algo) {
        System.out.println("\n" + algo + " Scheduling Results:");
        System.out.println("Process\tWaiting Time\tTurnaround Time");
        double totalWT = 0, totalTAT = 0;
        for (Process p : processes) {
            System.out.println(p.id + "\t" + p.waitingTime + "\t\t" + p.turnaroundTime);
            totalWT += p.waitingTime;
            totalTAT += p.turnaroundTime;
        }
        System.out.printf("Average Waiting Time: %.2f%n", totalWT / processes.size());
        System.out.printf("Average Turnaround Time: %.2f%n", totalTAT / processes.size());
    }
}