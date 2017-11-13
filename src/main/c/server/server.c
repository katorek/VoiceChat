/*
Run on linux,
Requires linux libraries
Run 'make' to compile
*/
#include <arpa/inet.h>
#include <netdb.h>
#include <unistd.h>
#include <sys/ioctl.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <netinet/in.h>
#include <net/if.h>
#include <pthread.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
//lab_sec_1
#define SERVER_PORT 12345
#define QUEUE_SIZE 5
#define MAX_CONNECTIONS 5
#define MAX_USERS 10

#define COLOR_UNDERLINE   "\x1b[4m"

#define COLOR_BBLACK   "\x1b[40m"
#define COLOR_BRED     "\x1b[41m"
#define COLOR_BGREEN   "\x1b[42m"
#define COLOR_BYELLOW  "\x1b[43m"
#define COLOR_BBLUE    "\x1b[44m"
#define COLOR_BMAGENTA "\x1b[45m"
#define COLOR_BCYAN    "\x1b[46m"

#define COLOR_RED     "\x1b[1;31m"
#define COLOR_GREEN   "\x1b[1;32m"
#define COLOR_YELLOW  "\x1b[1;33m"
#define COLOR_BLUE    "\x1b[1;34m"
#define COLOR_MAGENTA "\x1b[1;35m"
#define COLOR_CYAN    "\x1b[1;36m"
#define COLOR_RESET   "\x1b[0m"

struct users_arr
{
    char *username;
    char *password;
    int logged; //0 - not logged, 1 - logged already
};

int userCount = 0;
struct users_arr users[MAX_USERS];

//struktura zawierajÄca dane, ktĂłre zostanÄ przekazane do wÄtku
struct thread_data_t
{
    int conn_sck_desc;
};

int descs[MAX_CONNECTIONS];
pthread_mutex_t lista_mutex = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t active_users = PTHREAD_MUTEX_INITIALIZER;

void addUser(char *username, char *password){
    pthread_mutex_lock(&active_users);
    users[userCount].username=username;
    users[userCount].password=password;
    printf("New user:"COLOR_YELLOW"%s"COLOR_RESET", p:"COLOR_MAGENTA"%s\n"COLOR_RESET,users[userCount].username,users[userCount].password);
    ++userCount;
    pthread_mutex_unlock(&active_users);
}

int userExists(char *user){
   int i;
   for(i=0;i<userCount;++i){
        if(*users[i].username==*user && strlen(users[i].username)==strlen(user)) return 1;
   }
   return 0;
}

void wyslijZalogowanychUzytkownikow(){
    pthread_mutex_lock(&active_users);
    int i, j;
    for(i=0;i<userCount;++i){
        if(users[i].logged!=0){
            for(j=0;j<userCount;++j){
                if(users[j].logged!=0) {
                    printf("%s <- %s\n",users[i].username, users[j].username);
                    char buf[100];
                    memset(buf,0,100);
                    strcpy(buf,users[j].username);
                    buf[99]='9';
                    //todo copy z users[j].username do bufora
                    write(users[i].logged,buf,100);
                }
            }
        }
    }
    pthread_mutex_unlock(&active_users);
}

int logUser(char *user, char *pass, int socket){
    //todo wyslanie do wsyzstkich uzytkownikow listy z zalogowanymi uzytkownikami
    int i;

    for(i=0;i<userCount;++i){
//        printf("COMPARING\n%s %s\n%s %s\n",user,pass,users[i].username,users[i].password);
        if(*users[i].username==*user &&
        *users[i].password==*pass
        &&(strlen(users[i].username)==strlen(user))
        &&(strlen(users[i].password)==strlen(pass))){
            if(users[i].logged == 0) {
                users[i].logged = socket;
                return 1;
            }
            else return 2;
        }
    }
    return 0;
}

void dodajIdDoListy(int id){
    int i;
    pthread_mutex_lock(&lista_mutex);
    for(i=0;i<MAX_CONNECTIONS;++i){
        if(descs[i]==0){
            descs[i] = id;
            break;
        }
    }
    pthread_mutex_unlock(&lista_mutex);
}

void usunIdZListy(int id){
    int i;
    pthread_mutex_lock(&lista_mutex);
    for(i=0;i<MAX_CONNECTIONS;++i){
        if(descs[i]==id){
            descs[i] = 0;
            break;
        }
    }
    for(i=0;i<userCount;++i){
        if(users[i].logged == id){
            users[i].logged = 0;
            break;
        }
    }
    pthread_mutex_unlock(&lista_mutex);
}    

void wyslijDoListy(char messsage[]){
    if(messsage[0]!='0'){
        pthread_mutex_lock(&lista_mutex);
        int i;
        for(i=0;i<MAX_CONNECTIONS;++i){
            printf("%d\t",descs[i]);
            if(descs[i]!=0){
                write(descs[i],messsage,100);
            }
        }    
        printf("\n");
        pthread_mutex_unlock(&lista_mutex);
    }
}

void wyslijDoPozostalych(int socket,char messsage[]){
    if(messsage[0]!='0'){
        pthread_mutex_lock(&lista_mutex);
        int i;
        for(i=0;i<MAX_CONNECTIONS;++i){
            //printf("%d\t",descs[i]);
            if(descs[i]!=0 && descs[i]!=socket){
                write(descs[i],messsage,100);
            }
        }
        //printf("\n");
        pthread_mutex_unlock(&lista_mutex);
    }
}

void printIP(){
    int fd;
     struct ifreq ifr;

     fd = socket(AF_INET, SOCK_DGRAM, 0);

     /* I want to get an IPv4 IP address */
     ifr.ifr_addr.sa_family = AF_INET;

     /* I want IP address attached to "eth0" */
     strncpy(ifr.ifr_name, "eth0", IFNAMSIZ-1);

     ioctl(fd, SIOCGIFADDR, &ifr);

     close(fd);

     /* display result */
     printf(COLOR_YELLOW COLOR_UNDERLINE"IP:PORT\n"COLOR_RESET COLOR_GREEN"%s:%d\n"COLOR_RESET, inet_ntoa(((struct sockaddr_in *)&ifr.ifr_addr)->sin_addr),SERVER_PORT);
}

void *ThreadBehavior(void *t_data){
    pthread_detach(pthread_self());
    struct thread_data_t *th_data = (struct thread_data_t*)t_data;
    
    char bufor[100];
    memset(bufor,0,100);
    int readC = 0;
    int conn_sck = (*th_data).conn_sck_desc;
    
    printf("Connection "COLOR_GREEN"opened: "COLOR_MAGENTA"%d\n"COLOR_RESET,conn_sck);
    readC = read(conn_sck,bufor,100);
    sprintf(bufor,"%s",bufor);
    char *user = (char *)malloc(strlen(bufor)+1);
    char *pass = (char *)malloc(strlen(bufor)+1);

    char choice = bufor[0];

    int i = 1;
    while(bufor[i]!=';')++i;
    strncpy(user,bufor+1,i-1);
    i++;

    int j=0;
    while(bufor[j+i]!=';')++j;
    strncpy(pass,bufor+i,j);
    int loggedProperly = 1;

    memset(bufor,0,100);
    //5 new user
    if(choice=='5') {
        if(userExists(user)==1){//exists
            printf(COLOR_RED"User exists!\n"COLOR_RESET);
            bufor[1]='2';
            loggedProperly=0;
        }else{
            bufor[0]='1';
            addUser(user,pass);
            logUser(user,pass,conn_sck);
        }
    }
    if(choice=='4'){
        printf(COLOR_YELLOW"%s"COLOR_RESET" -> ",user);
        int logStatus = logUser(user,pass,conn_sck);
        if(logStatus==1){
            bufor[0]='1';
            printf(COLOR_GREEN"login success\n"COLOR_RESET);//dobre logowanie
        }else if(logStatus==2){
            loggedProperly=0;
            bufor[0]='3';
            printf(COLOR_RED"already logged\n"COLOR_RESET);//dobre logowanie
        }
        else{
            loggedProperly=0;
            bufor[0]='2';
            printf(COLOR_RED"login failed\n"COLOR_RESET);//zle logowanie
        }
    }

    bufor[1]='\n';

    sprintf(bufor,"%s",bufor);
    write(conn_sck,bufor,100);

    if(loggedProperly == 1){
        wyslijZalogowanychUzytkownikow();
        while((readC = read(conn_sck, bufor, 99))>0){
            sprintf(bufor,"%s",bufor);
    //        printf("%s\n",bufor);
            wyslijDoPozostalych(conn_sck,bufor);
        }
    }

    printf("Connection "COLOR_RED"closed:"COLOR_MAGENTA"%d\n"COLOR_RESET,conn_sck);
    bufor[0]= '0';
    write(conn_sck, bufor,100);
    free(th_data);
    usunIdZListy(conn_sck);
    pthread_exit(NULL);
}

void handleConnection(int connection_socket_descriptor) {
    dodajIdDoListy(connection_socket_descriptor);
    int create_result = 0;
    pthread_t thread1;

    struct thread_data_t *t_data;
    t_data = malloc(sizeof t_data);
    t_data->conn_sck_desc = connection_socket_descriptor;
    
    create_result = pthread_create(&thread1, NULL,   ThreadBehavior, (void *)t_data);
    if (create_result){
       printf("BĹÄd przy prĂłbie utworzenia wÄtku, kod bĹÄdu: %d\n", create_result);
       exit(-1);
    }
}

int main(int argc, char* argv[])
{
    addUser("wojtek","12345");
    addUser("guest","123");

    printIP();

    printf("Running Voice Chat Server V1.0 by "COLOR_CYAN COLOR_UNDERLINE"Kator\n"COLOR_RESET);

    int tempI=0;
    for(tempI=0;tempI < MAX_CONNECTIONS; ++tempI){
        descs[tempI]= 0;
    }
    int server_socket_descriptor;
   int connection_socket_descriptor;
   int bind_result;
   int listen_result;
   char reuse_addr_val = 1;
   struct sockaddr_in server_address;

   //inicjalizacja gniazda serwera
   
   memset(&server_address, 0, sizeof(struct sockaddr));
   server_address.sin_family = AF_INET;
   server_address.sin_addr.s_addr = htonl(INADDR_ANY);
   server_address.sin_port = htons(SERVER_PORT);

   server_socket_descriptor = socket(AF_INET, SOCK_STREAM, 0);
   if (server_socket_descriptor < 0)
   {
       fprintf(stderr, "%s: BĹÄd przy prĂłbie utworzenia gniazda..\n", argv[0]);
       exit(1);
   }
   setsockopt(server_socket_descriptor, SOL_SOCKET, SO_REUSEADDR, (char*)&reuse_addr_val, sizeof(reuse_addr_val));

   bind_result = bind(server_socket_descriptor, (struct sockaddr*)&server_address, sizeof(struct sockaddr));
   if (bind_result < 0)
   {
       fprintf(stderr, "%s: BĹÄd przy prĂłbie dowiÄzania adresu IP i numeru portu do gniazda.\n", argv[0]);
       exit(1);
   }

   listen_result = listen(server_socket_descriptor, QUEUE_SIZE);
   if (listen_result < 0) {
       fprintf(stderr, "%s: BĹÄd przy prĂłbie ustawienia wielkoĹci kolejki.\n", argv[0]);
       exit(1);
   }

   while(1)
   {
       connection_socket_descriptor = accept(server_socket_descriptor, NULL, NULL);
       if (connection_socket_descriptor < 0)
       {
           fprintf(stderr, "%s: BĹÄd przy prĂłbie utworzenia gniazda dla poĹÄczenia.\n", argv[0]);
           exit(1);
       }

       handleConnection(connection_socket_descriptor);
   }

   close(server_socket_descriptor);
   return(0);
}
