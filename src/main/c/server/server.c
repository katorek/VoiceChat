/*
Run on linux,
Requires linux libraries
Run 'make' to compile
*/
#include <sys/types.h>
#include <sys/wait.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <netdb.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include <pthread.h>
//lab_sec_1
#define SERVER_PORT 12345
#define QUEUE_SIZE 5
#define MAX_CONNECTIONS 5
#define MAX_USERS 10

struct users_arr
{
    char *username;
    char *password;
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

void addUser(char *username, char *password){
    users[userCount].username=username;
    users[userCount].password=password;

//    strcpy(users[userCount].username,username);
//    strcpy(users[userCount].password,password);
    printf("New user:%s, p:%s\n",users[userCount].username,users[userCount].password);
    ++userCount;
}

int userExists(char *user){
   int i;
   for(i=0;i<userCount;++i){
        if(*users[i].username==*user && strlen(users[i].username)==strlen(user)) return 1;
   }
   return 0;
}

int logUser(char *user, char *pass){
    int i;
    for(i=0;i<userCount;++i){
//        printf("COMPARING\n%s %s\n%s %s\n",user,pass,users[i].username,users[i].password);
        if(*users[i].username==*user &&
        *users[i].password==*pass
        &&(strlen(users[i].username)==strlen(user))
        &&(strlen(users[i].password)==strlen(pass))) return 1;
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


void *ThreadBehavior(void *t_data)
{
    pthread_detach(pthread_self());
    struct thread_data_t *th_data = (struct thread_data_t*)t_data;
    
    char bufor[100];
    memset(bufor,0,100);
    int readC = 0;
    int conn_sck = (*th_data).conn_sck_desc;
    
    printf("New connection on:%d\n",conn_sck);
    readC = read(conn_sck,bufor,100);
    sprintf(bufor,"%s",bufor);
    char *user = (char *)malloc(strlen(bufor)+1);
    char *pass = (char *)malloc(strlen(bufor)+1);

    char choice = bufor[0];
    printf("c:%c\n",choice);

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
            printf("User exists!\n");
            bufor[1]='2';
            loggedProperly=0;
        }else{
            bufor[0]='1';
            addUser(user,pass);
//            logUser(user,pass);
        }
    }
    if(choice=='4'){
        printf("4: ");
        if(logUser(user,pass)==1){
            bufor[0]='1';
            printf("login success\n");//dobre logowanie
        }
        else{
            loggedProperly=0;
            bufor[0]='2';
            printf("login failed\n");//zle logowanie
        }
    }

//    printf("User: %s logged in.\n",user);
    bufor[1]='\n';

    sprintf(bufor,"%s",bufor);
    write(conn_sck,bufor,100);

    if(loggedProperly == 1){
        while((readC = read(conn_sck, bufor, 100))>0){
            sprintf(bufor,"%s",bufor);
    //        printf("%s\n",bufor);
            wyslijDoPozostalych(conn_sck,bufor);
        }
    }

    printf("Connection closed on:%d\n",conn_sck); 
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
    printf("Running Voice Chat Server V1.0 by Kator\n");
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
