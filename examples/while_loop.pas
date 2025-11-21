program loops;
var i, sum: integer;

begin
    sum := 0;
    i := 1;
    
    while i < 6 do
    begin
        sum := sum + i;
        i := i + 1
    end;
    
    writeln(sum)
end.
